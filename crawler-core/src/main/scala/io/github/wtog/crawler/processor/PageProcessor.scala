package io.github.wtog.crawler.processor

import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.LinkedBlockingQueue

import io.github.wtog.crawler.actor.ExecutionContexts.processorDispatcher
import io.github.wtog.crawler.pipeline.{ ConsolePipeline, Pipeline }
import io.github.wtog.crawler.queue.TargetRequestTaskQueue
import io.github.wtog.crawler.selector.HtmlParser
import io.github.wtog.crawler.selector.HtmlParser.parseJson

import scala.concurrent.Future
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
  def process(page: Page): Future[Unit] = Future {
    doProcess(page)
  }

  protected def doProcess(page: Page): Unit

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

case class Page(isDownloadSuccess: Boolean = true, bytes: Option[Array[Byte]] = None, responseHeaders: Map[String, String] = Map.empty[String, String], requestSetting: RequestSetting) {

  lazy val resultItems: LinkedBlockingQueue[Any] = new LinkedBlockingQueue[Any]
  lazy val requestQueue: TargetRequestTaskQueue  = new TargetRequestTaskQueue()

  lazy val url = requestSetting.url.get

  def source: String = bytes match {
    case Some(byte) ⇒
      HtmlParser.getHtmlSourceWithCharset(byte, requestSetting.charset)
    case None ⇒
      throw new IllegalStateException("no page source text found ")
  }

  def json[T: Manifest](text: Option[String] = None) = parseJson[T](text.getOrElse(this.source))

  def addTargetRequest(urlAdd: String): Unit = addRequest(this.requestSetting.withUrl(url = urlAdd))

  def addTargetRequest(requestUri: RequestUri): Unit = addRequest(this.requestSetting.withRequestUri(requestUri))

  private[this] def addRequest(requestSetting: RequestSetting): Unit = {
    val url = requestSetting.url.get

    if (Try(new URL(url)).isSuccess) {
      this.requestQueue.push(requestSetting)
    }
  }

  def addPageResultItem[R](result: R): Unit = this.resultItems.add(result)

  override def toString: String = s"${requestSetting.url.get} downloaded ${isDownloadSuccess}"
}

case class RequestUri(url: String, method: String, requestBody: Option[String] = None, headers: Option[Map[String, String]] = None)

case class RequestSetting(
    domain: String = "",
    method: String = "GET",
    url: Option[String] = None,
    userAgent: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.75 Safari/537.36",
    requestBody: Option[String] = None,
    headers: Map[String, String] = Map.empty[String, String],
    sleepTime: Duration = 1 seconds,
    cookies: Option[Map[String, String]] = None,
    charset: String = Charset.defaultCharset().name(),
    retryTime: Int = 0,
    timeOut: Duration = 3 seconds,
    useProxy: Boolean = false) {

  def withUrlAndMethod(url: String, method: String = "GET") =
    this.copy(url = Some(url), method = method)

  def withUrl(url: String) = this.copy(url = Some(url))

  def withSleepTime(sleepTime: Duration) = this.copy(sleepTime = sleepTime)

  def withHeaders(extraHeaders: Map[String, String]) =
    this.copy(
      headers = extraHeaders.foldLeft(this.headers)((common, extra) ⇒ common + extra)
    )

  def withMethodAndRequestBody(method: String, requestBody: Option[String]) =
    this.copy(method = method, requestBody = requestBody)

  def withRequestUri(requestUri: RequestUri) = {
    val basic = this.copy(
      url = Some(requestUri.url),
      method = requestUri.method,
      requestBody = requestUri.requestBody
    )

    requestUri.headers.fold(basic) { extra ⇒
      basic.withHeaders(extra)
    }
  }

  override def toString: String = {
    val fields = this.getClass.getDeclaredFields
      .map { field =>
        val value = field.get(this) match {
          case v: Option[Any] =>
            v.getOrElse("")
          case v =>
            v
        }

        (s"${field.getName}: $value", value)
      }
      .collect {
        case (v: String, t: String) if !t.isEmpty           => v
        case (v: String, t: Any) if !t.isInstanceOf[String] => v
      }

    s"${fields.mkString(", ")}"
  }
}
