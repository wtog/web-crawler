package io.github.wtog.processor

import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.LinkedBlockingQueue

import io.github.wtog.pipeline.{ ConsolePipeline, Pipeline }
import io.github.wtog.queue.TargetRequestTaskQueue
import io.github.wtog.selector.HtmlParser
import io.github.wtog.utils.CharsetUtils

import scala.concurrent.duration._
import scala.util.Try

/**
 * @author : tong.wang
 * @since : 5/16/18 9:48 PM
 * @version : 1.0.0
 */
trait PageProcessor extends HtmlParser {

  /**
   * the target urls for processor to crawl
   *
   * @return
   */
  def targetUrls: List[String]

  /**
   * handle the crawled result
   *
   * @return
   */
  def pipelines: Set[Pipeline] = Set(ConsolePipeline)

  /**
   * parse the html source code
   *
   * @param page
   */
  def process(page: Page)

  /**
   * set request config for processor
   *
   * @return
   */
  def requestSetting: RequestSetting = RequestSetting(url = None)

  /**
   * schedule cron job expression
   *
   * @return
   */
  def cronExpression: Option[String] = None
}

case class Page(
    isDownloadSuccess: Boolean             = true,
    bytes:             Option[Array[Byte]] = None,
    responseHeaders:   Map[String, String] = Map("Content-Type" -> Charset.defaultCharset().toString),
    requestSetting:    RequestSetting) {

  lazy val resultItems: LinkedBlockingQueue[Map[String, Any]] = new LinkedBlockingQueue
  lazy val requestQueue: TargetRequestTaskQueue = new TargetRequestTaskQueue()

  val url = requestSetting.url.get

  def source: String = bytes match {
    case Some(byte) ⇒
      CharsetUtils.getHtmlSourceWithCharset(Some(responseHeaders("Content-Type")), byte)
    case None ⇒
      throw new IllegalStateException("no page source text found ")
  }

  def addTargetRequest(urlAdd: String): Unit = {
    addRequest(this.requestSetting.withUrl(url = urlAdd))
  }

  def addTargetRequest(requestUri: RequestUri): Unit = {
    addRequest(this.requestSetting.withRequestUri(requestUri))
  }

  private[this] def addRequest(requestSetting: RequestSetting): Unit = {
    val url = requestSetting.url.get

    if (Try(new URL(url)).isSuccess) {
      this.requestQueue.push(requestSetting)
    }
  }

  def addPageResultItem(result: Map[String, Any]): Unit = {
    this.resultItems.add(result)
  }

  override def toString: String = s"${requestSetting.url.get} downloaded ${isDownloadSuccess}"
}

case class RequestUri(url: String, method: String, requestBody: Option[String] = None, headers: Option[Map[String, String]] = None)

case class RequestSetting(
    url:                     Option[String]              = None,
    domain:                  String                      = "",
    method:                  String                      = "GET",
    requestBody:             Option[String]              = None,
    headers:                 Map[String, String]         = Map.empty[String, String],
    sleepTime:               Duration                    = 1 seconds,
    userAgent:               String                      = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36",
    cookies:                 Option[Map[String, String]] = None,
    charset:                 Option[String]              = Some("UTF-8"),
    retryTimes:              Int                         = 0,
    cycleRetryTimes:         Int                         = 0,
    retrySleepTime:          Int                         = 1000,
    timeOut:                 Int                         = 3000,
    useGzip:                 Boolean                     = true,
    disableCookieManagement: Boolean                     = false,
    useProxy:                Boolean                     = false) {

  def withUrlAndMethod(url: String, method: String = "GET") = this.copy(url = Some(url), method = method)

  def withUrl(url: String) = this.copy(url = Some(url))

  def withSleepTime(sleepTime: Duration) = this.copy(sleepTime = sleepTime)

  def withHeaders(extraHeaders: Map[String, String]) = this.copy(headers = extraHeaders.foldLeft(this.headers)((common, extra) ⇒ common + extra))

  def withMethodAndRequestBody(method: String, requestBody: Option[String]) = this.copy(method = method, requestBody = requestBody)

  def withRequestUri(requestUri: RequestUri) = {
    val basic = this.copy(url = Some(requestUri.url), method = requestUri.method, requestBody = requestUri.requestBody)

    requestUri.headers.fold(basic) { extra ⇒
      basic.withHeaders(extra)
    }
  }
}
