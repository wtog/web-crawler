package io.github.wtog.crawler.dto

import java.net.URL
import java.util.concurrent.LinkedBlockingQueue

import io.github.wtog.crawler.queue.TargetRequestTaskQueue
import io.github.wtog.crawler.selector.HtmlParser
import io.github.wtog.crawler.selector.HtmlParser.parseJson
import org.apache.logging.log4j.scala.Logging

import scala.util.Try

/**
  * @author : tong.wang
  * @since : 1/2/20 9:41 PM
  * @version : 1.0.0
  */
case class Page(
    isDownloadSuccess: Boolean = true,
    bytes: Option[Array[Byte]] = None,
    responseHeaders: Map[String, String] = Map.empty[String, String],
    xhrResponses: Seq[XhrResponse] = Seq.empty[XhrResponse],
    requestSetting: RequestSetting) {

  lazy val resultItems: LinkedBlockingQueue[Any] = new LinkedBlockingQueue[Any]
  lazy val requestQueue: TargetRequestTaskQueue  = new TargetRequestTaskQueue()

  lazy val url = requestSetting.url.get

  def source: String = bytes match {
    case Some(byte) ⇒
      HtmlParser.getHtmlSourceWithCharset(byte, requestSetting.charset)
    case None ⇒
      throw new IllegalStateException("no page source text found ")
  }

  def json[T: Manifest](text: Option[String] = None): T = parseJson[T](text.getOrElse(this.source))

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

object Page extends Logging {
  def failed(requestSetting: RequestSetting, exceptionMessage: Throwable): Page = {
    logger.warn(s"failed to download cause ${exceptionMessage.getLocalizedMessage}")
    Page(requestSetting = requestSetting, isDownloadSuccess = false)
  }
}

case class XhrResponse(xhrUri: String, result: Map[String, Any])
