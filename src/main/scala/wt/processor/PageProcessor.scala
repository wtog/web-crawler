package wt.processor

import java.util.concurrent.LinkedBlockingQueue

import wt.downloader.{RequestHeaderGeneral, RequestHeaders}
import wt.queue.{LinkQueue, RequestQueue}
import wt.selector.HtmlParser


/**
  * @author : tong.wang
  * @since : 5/16/18 9:48 PM
  * @version : 1.0.0
  */
trait PageProcessor {
  def process(page: Page)

  def requestHeaders: RequestHeaders
}

case class Page(
  isDownloadSuccess: Boolean = false,
  pageSource: Option[String] = None,
  requestGeneral: RequestHeaderGeneral) {

  lazy val resultItems: (LinkedBlockingQueue[Any], Int) = (new LinkedBlockingQueue, 100)
  lazy val requestQueue: (RequestQueue, Int) = (new LinkQueue(), 100)

  lazy val jsoupParser = HtmlParser(pageSource.getOrElse(throw new IllegalArgumentException("pageSource is empty")), requestGeneral.url.get).JsoupParser.document.get


  def addTargetRequest(urlAdd: String): Unit = {
    this.requestQueue._1.push(RequestHeaderGeneral(url = Some(urlAdd)))
  }

  def addPageResultItem(result: Any) = {
    this.resultItems._1.add(result)
  }

}