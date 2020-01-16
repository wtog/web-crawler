package io.github.wtog.crawler.downloader

import java.io.File
import java.util
import java.util.logging.Level

import io.github.wtog.crawler.downloader.ChromeHeadlessConfig._
import io.github.wtog.crawler.dto.{ Page, RequestSetting, XhrResponse }
import io.github.wtog.utils.{ ConfigUtils, JsonUtils }
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeOptions }
import org.openqa.selenium.logging.{ LogType, LoggingPreferences }
import org.openqa.selenium.remote.UnreachableBrowserException

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
  * @author : tong.wang
  * @since : 2019-07-18 22:28
  * @version : 1.0.0
  */
object ChromeHeadlessDownloader extends Downloader[ChromeDriver] {

  override protected def doDownload(requestSetting: RequestSetting): Future[Page] = {

    val client = getOrCreateClient(requestSetting)

    Future {
      try {
        val driver = client.driver

        driver.get(requestSetting.url.get)
        val performanceLog = driver.manage().logs().get(LogType.PERFORMANCE)

        val iterator            = performanceLog.iterator()
        var returnAllXhrRequest = true
        val xhrResponseBuffer   = new ListBuffer[XhrResponse]
        while (iterator.hasNext && returnAllXhrRequest) {
          val xhrResponse = iterator.next()
          val message     = JsonUtils.parseFrom[Map[String, Any]](xhrResponse.getMessage).get("message").get.asInstanceOf[Map[String, Any]]
          message.get("params").foreach {
            case params: Map[String, Any] =>
              val headers = params.getOrElse("headers", Map.empty[String, Any]).asInstanceOf[Map[String, Any]]
              getXhrRequestUriByHeaders(headers).foreach {
                case xhrResponseUri: String if requestSetting.xhrRequests.contains(xhrResponseUri) =>
                  xhrResponseBuffer.append(XhrResponse(xhrResponseUri, getXhrResponse(driver, params.get("requestId").get.asInstanceOf[String])))
                case _ if (xhrResponseBuffer.size == requestSetting.xhrRequests.size) =>
                  returnAllXhrRequest = false
                case _ =>
              }
          }
        }

        Page(requestSetting = requestSetting, xhrResponses = xhrResponseBuffer)
      } catch {
        case NonFatal(exception) =>
          Page.failed(requestSetting, exception)
      } finally {
        client.decrement()
      }
    }(io.github.wtog.crawler.actor.ExecutionContexts.downloadDispatcher)
  }

  private def getXhrResponse(driver: ChromeDriver, requestId: String): Map[String, Any] = {
    val cdpMap = new util.HashMap[String, Object]()
    cdpMap.put("requestId", requestId)
    driver.executeCdpCommand("Network.getResponseBody", cdpMap).asScala.toMap
  }

  private def getXhrRequestUriByHeaders(headers: Map[String, Any]): Option[String] =
    headers.get("x-requested-with") match {
      case Some("XMLHttpRequest") =>
        val schema: String = headers.getOrElse(":scheme", "").asInstanceOf[String]
        val domain: String = headers.getOrElse(":authority", "").asInstanceOf[String]
        val uri: String = headers.get(":path").fold[String]("") {
          case p: String =>
            val queryIndex = p.indexOf('?')
            p.substring(0, if (queryIndex > 0) queryIndex else p.length)
        }

        Some(s"$schema://$domain$uri")
      case _ =>
        None
    }

  private[this] def buildOptions(requestSetting: RequestSetting): ChromeOptions = {
    val options = new ChromeOptions()
    options.setExperimentalOption("excludeSwitches", Array[String]("enable-automation"))

    val perf = new util.HashMap[String, Any]()
    perf.put("enableNetwork", true)
    options.setExperimentalOption("prefs", perf)

    val logPrefs = new LoggingPreferences
    logPrefs.enable(LogType.PERFORMANCE, Level.ALL)
    options.setCapability("goog:loggingPrefs", logPrefs)
    options.addArguments(
      "--no-sandbox",
      "--headless",
      "--disable-dev-shm-usage",
      "--disable-gpu",
      "--ignore-certificate-errors",
      s"--user-agent=${requestSetting.userAgent}"
    )

    options
  }

  override def getOrCreateClient(requestSetting: RequestSetting): DownloaderClient[ChromeDriver] = getDownloaderClient(requestSetting.domain) {
    System.setProperty("webdriver.chrome.driver", chromeDriverPath)
    System.setProperty("webdriver.chrome.logfile", chromeDriverLog)

    val driver = new ChromeDriver(buildOptions(requestSetting))
    val map    = new util.HashMap[String, Object]()
    map.put("source", """
        |Object.defineProperty(navigator, 'webdriver', {
        |      get: () => false,
        |});
        |""".stripMargin)
    driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", map)

    driver
  }

  override def closeClient(): Unit = closeDownloaderClient { driver =>
    try (driver.quit())
    catch {
      case _: UnreachableBrowserException =>
        Unit
      case e: Throwable =>
        throw e
    }
  }

}

object ChromeHeadlessConfig {
  lazy val chromeDriverPath: String = ConfigUtils.getStringOpt("crawler.chrome.driver").getOrElse("/opt/chromedriver")
  lazy val chromeDriverLog: String  = ConfigUtils.getStringOpt("crawler.chrome.log").getOrElse("/tmp/chromedriver.log")

  def chromeDriverNotExecutable: Boolean = {
    val file       = new File(chromeDriverPath)
    val canExecute = file.exists() && file.canExecute
    !canExecute
  }
}
