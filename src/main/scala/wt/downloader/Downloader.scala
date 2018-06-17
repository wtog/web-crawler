package wt.downloader

import org.slf4j.{Logger, LoggerFactory}
import wt.Spider
import wt.processor.Page

import scala.concurrent.Future

/**
  * @author : tong.wang
  * @since : 5/16/18 9:56 PM
  * @version : 1.0.0
  */
trait Downloader {
  protected lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def download(request: RequestHeaders): Future[Page]
}

case class RequestHeaderGeneral(
   method: String = "GET",
   url: Option[String],
   requestBody: Option[String] = None)

case class RequestHeaders(
   domain: String,
   requestHeaderGeneral: Option[RequestHeaderGeneral] = None,
   userAgent: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36",
   headers: Option[Map[String, String]] = None,
   cookies: Option[Map[String, String]] = None,
   charset: Option[String] = Some("UTF-8"),
   sleepTime: Int = 5000,
   retryTimes: Int = 0,
   cycleRetryTimes: Int = 0,
   retrySleepTime: Int = 1000,
   timeOut: Int = 5000,
   useGzip: Boolean = true,
   disableCookieManagement: Boolean = false,
   useProxy: Boolean = false)

