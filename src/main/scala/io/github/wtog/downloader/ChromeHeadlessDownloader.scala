package io.github.wtog.downloader

import java.io.File

import io.github.wtog.downloader.ChromeHeadlessConfig._
import io.github.wtog.processor.{ Page, RequestSetting }
import io.github.wtog.utils.ConfigUtils
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeOptions }
import org.openqa.selenium.remote.UnreachableBrowserException

import scala.concurrent.Future
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
        val pageSource = driver.getPageSource
        pageResult(requestSetting = requestSetting, results = Some(pageSource.getBytes))
      } catch {
        case NonFatal(exception) =>
          pageResult(requestSetting, None, downloadSuccess = false, msg = Some(exception.getLocalizedMessage))
      } finally {
        client.decrement()
      }
    }(io.github.wtog.actor.ExecutionContexts.downloadDispatcher)
  }

  override def getOrCreateClient(requestSetting: RequestSetting) = getDownloaderClient(requestSetting.domain) {
    System.setProperty("webdriver.chrome.driver", chromeDriverPath)
    System.setProperty("webdriver.chrome.logfile", chromeDriverLog)
    val options = new ChromeOptions()
    options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200", "--ignore-certificate-errors")

    new ChromeDriver(options)
  }

  override def closeClient() = closeDownloaderClient { driver =>
    try (driver.quit())
    catch {
      case e: UnreachableBrowserException =>
        Unit
      case e: Throwable =>
        throw e
    }
  }

}

object ChromeHeadlessConfig {
  lazy val chromeDriverPath = ConfigUtils.getStringOpt("web-crawler.chrome.driver").getOrElse("/opt/chromedriver")
  lazy val chromeDriverLog  = ConfigUtils.getStringOpt("web-crawler.chrome.log").getOrElse("/tmp/chromedriver.log")

  def chromeDriverNotExecutable = {
    val file       = new File(chromeDriverPath)
    val canExecute = file.exists() && file.canExecute
    !canExecute
  }
}
