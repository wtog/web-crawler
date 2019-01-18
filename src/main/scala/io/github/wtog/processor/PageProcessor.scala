package io.github.wtog.processor

import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.LinkedBlockingQueue

import io.github.wtog.pipeline.{ ConsolePipeline, Pipeline }
import io.github.wtog.queue.{ LinkQueue, RequestQueue }
import io.github.wtog.selector.HtmlParser
import io.github.wtog.utils.CharsetUtils

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
   * set RequestHeaders For processor
   * ps: RequestHeaders(domain = "www.baidu.com")
   *
   * @return
   */
  def requestHeaders: RequestHeaders

  /**
   * schedule cron job expression
   * @return
   */
  def cronExpression: Option[String] = None
}

case class Page(
    isDownloadSuccess: Boolean              = true,
    bytes:             Option[Array[Byte]]  = None,
    responseHeaders:   Map[String, String]  = Map("Content-Type" -> Charset.defaultCharset().toString),
    requestGeneral:    RequestHeaderGeneral) {

  lazy val resultItems: LinkedBlockingQueue[Map[String, Any]] = new LinkedBlockingQueue
  lazy val requestQueue: RequestQueue = new LinkQueue()

  val url = requestGeneral.url.get

  def source: String = bytes match {
    case Some(byte) ⇒
      CharsetUtils.getHtmlSourceWithCharset(Some(responseHeaders("Content-Type")), byte)
    case None ⇒
      throw new IllegalStateException("no page source text found ")
  }

  def addTargetRequest(urlAdd: String): Unit = {
    addTargetRequest(RequestHeaderGeneral(url = Some(urlAdd)))
  }

  def addTargetRequest(requestHeaderGeneral: RequestHeaderGeneral, requestBody: Option[String] = None): Unit = {
    val url = requestHeaderGeneral.url.get

    if (Try(new URL(url)).isSuccess)
      this.requestQueue.push(RequestHeaderGeneral(url = Some(url), requestBody = requestBody))
  }

  def addPageResultItem(result: Map[String, Any]): Unit = {
    this.resultItems.add(result)
  }

  override def toString: String = s"${requestGeneral.url.get} downloaded ${isDownloadSuccess}"
}

case class RequestHeaderGeneral(
    method:      String                      = "GET",
    url:         Option[String],
    requestBody: Option[String]              = None,
    headers:     Option[Map[String, String]] = None)

case class RequestHeaders(
    domain:                  String,
    requestHeaderGeneral:    Option[RequestHeaderGeneral] = None,
    userAgent:               String                       = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36",
    commonHeaders:           Map[String, String]          = Map.empty[String, String],
    cookies:                 Option[Map[String, String]]  = None,
    charset:                 Option[String]               = Some("UTF-8"),
    sleepTime:               Int                          = 3000,
    retryTimes:              Int                          = 0,
    cycleRetryTimes:         Int                          = 0,
    retrySleepTime:          Int                          = 1000,
    timeOut:                 Int                          = 3000,
    useGzip:                 Boolean                      = true,
    disableCookieManagement: Boolean                      = false,
    useProxy:                Boolean                      = false) {

  val headers = this.requestHeaderGeneral.foldLeft(this.commonHeaders) {
    (commonHeaders, HeaderGeneral) ⇒ commonHeaders ++ HeaderGeneral.headers.getOrElse(Map.empty[String, String])
  }
}
