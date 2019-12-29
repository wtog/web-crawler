package io.github.wtog.crawler.downloader

import java.io.File

import io.github.wtog.crawler.downloader.ChromeHeadlessConfig._
import io.github.wtog.crawler.processor.{ Page, RequestSetting }
import io.github.wtog.utils.ConfigUtils
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeOptions }
import org.openqa.selenium.remote.UnreachableBrowserException

import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

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
        //fix
        val pageSource = Try(driver.findElementByTagName("pre").getText).getOrElse(driver.getPageSource)
        pageResult(requestSetting = requestSetting, results = Some(pageSource.getBytes))
      } catch {
        case NonFatal(exception) =>
          pageResult(requestSetting, None, downloadSuccess = false, msg = Some(exception.getLocalizedMessage))
      } finally {
        client.decrement()
      }
    }(io.github.wtog.crawler.actor.ExecutionContexts.downloadDispatcher)
  }

  override def getOrCreateClient(requestSetting: RequestSetting): DownloaderClient[ChromeDriver] = getDownloaderClient(requestSetting.domain) {
    System.setProperty("webdriver.chrome.driver", chromeDriverPath)
    System.setProperty("webdriver.chrome.logfile", chromeDriverLog)
    val options = new ChromeOptions()

    options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200", "--ignore-certificate-errors")

    new ChromeDriver(options)
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
