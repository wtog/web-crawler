package io.github.wtog.processor

import java.nio.charset.Charset
import java.util.concurrent.LinkedBlockingQueue

import io.github.wtog.pipeline.{ ConsolePipeline, Pipeline }
import io.github.wtog.queue.{ LinkQueue, RequestQueue }
import io.github.wtog.selector.HtmlParser
import io.github.wtog.utils.CharsetUtils

/**
 * @author : tong.wang
 * @since : 5/16/18 9:48 PM
 * @version : 1.0.0
 */
trait PageProcessor {

  /**
   * the target urls for processor to crawl
   * @return
   */
  def targetUrls: List[String]

  /**
   * handle the crawled result
   * @return
   */
  def pipelines: Set[Pipeline] = Set(ConsolePipeline)

  /**
   * parse the html source code
   * @param page
   */
  def process(page: Page)

  /**
   * set RequestHeaders For processor
   * ps: RequestHeaders(domain = "www.baidu.com")
   * @return
   */
  def requestHeaders: RequestHeaders
}

case class Page(
    isDownloadSuccess: Boolean              = true,
    bytes:             Option[Array[Byte]]  = None,
    responseHeaders:   Map[String, String]  = Map("Content-Type" -> Charset.defaultCharset().toString),
    requestGeneral:    RequestHeaderGeneral) {

  lazy val resultItems: LinkedBlockingQueue[Map[String, Any]] = new LinkedBlockingQueue
  lazy val requestQueue: RequestQueue = new LinkQueue()

  lazy val jsoupParser = HtmlParser(pageSource, requestGeneral.url.get).document

  def pageSource: Option[String] = {
    bytes match {
      case Some(b) ⇒
        CharsetUtils.detectCharset(Some(responseHeaders("Content-Type")), b) match {
          case (_, c @ Some(_))      ⇒ c
          case (actualCharset, None) ⇒ Option(new String(b, actualCharset))
        }
      case None ⇒ None
    }
  }

  def addTargetRequest(urlAdd: String): Unit = {
    this.requestQueue.push(RequestHeaderGeneral(url = Some(urlAdd)))
  }

  def addPageResultItem(result: Map[String, Any]) = {
    this.resultItems.add(result)
  }

  override def toString: String = {
    s"${requestGeneral.url.get} downloaded ${isDownloadSuccess}"
  }
}

case class RequestHeaderGeneral(
    method:      String         = "GET",
    url:         Option[String],
    requestBody: Option[String] = None)

case class RequestHeaders(
    domain:                  String,
    requestHeaderGeneral:    Option[RequestHeaderGeneral] = None,
    userAgent:               String                       = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36",
    headers:                 Option[Map[String, String]]  = None,
    cookies:                 Option[Map[String, String]]  = None,
    charset:                 Option[String]               = Some("UTF-8"),
    sleepTime:               Int                          = 2000,
    retryTimes:              Int                          = 0,
    cycleRetryTimes:         Int                          = 0,
    retrySleepTime:          Int                          = 1000,
    timeOut:                 Int                          = 5000,
    useGzip:                 Boolean                      = true,
    disableCookieManagement: Boolean                      = false,
    useProxy:                Boolean                      = false)
