package wt.processor

import java.nio.charset.Charset
import java.util.concurrent.LinkedBlockingQueue

import wt.utils.CharsetUtils
import wt.downloader.{RequestHeaderGeneral, RequestHeaders}
import wt.pipeline.{ConsolePipeline, Pipeline}
import wt.queue.{LinkQueue, RequestQueue}
import wt.selector.HtmlParser


/**
  * @author : tong.wang
  * @since : 5/16/18 9:48 PM
  * @version : 1.0.0
  */
trait PageProcessor {
  def targetUrls: List[String]

  def pipelines: Set[Pipeline] = Set(ConsolePipeline)

  def process(page: Page)

  def requestHeaders: RequestHeaders
}

case class Page(
  isDownloadSuccess: Boolean = false,
  bytes: Option[Array[Byte]] = None,
  responseHeaders: Map[String, String] = Map("Content-Type" -> Charset.defaultCharset().toString),
  requestGeneral: RequestHeaderGeneral) {

  lazy val resultItems: (LinkedBlockingQueue[Map[String, Any]], Int) = (new LinkedBlockingQueue, 500)
  lazy val requestQueue: (RequestQueue, Int) = (new LinkQueue(), 100)

  lazy val jsoupParser = HtmlParser(pageSource.getOrElse(throw new IllegalArgumentException("pageSource is empty")), requestGeneral.url.get).JsoupParser.document.get

  def pageSource: Option[String] = {
    bytes match {
      case Some(b) =>
        CharsetUtils.detectCharset(Some(responseHeaders("Content-Type")), b) match {
          case (_, c @ Some(_)) => c
          case (actualCharset, None) => Option(new String(b, actualCharset))
        }
      case None => None
    }
  }

  def addTargetRequest(urlAdd: String): Unit = {
    this.requestQueue._1.push(RequestHeaderGeneral(url = Some(urlAdd)))
  }

  def addPageResultItem(result: Map[String, Any]) = {
    this.resultItems._1.add(result)
  }

  override def toString: String = {
    s"${requestGeneral.url.get} downloaded ${isDownloadSuccess}"
  }
}