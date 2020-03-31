package io.github.wtog.crawler.processor

import io.github.wtog.crawler.actor.ExecutionContexts.processorDispatcher
import io.github.wtog.crawler.downloader.{ AsyncHttpClientDownloader, Downloader }
import io.github.wtog.crawler.dto.{ Page, RequestSetting, RequestUri }
import io.github.wtog.crawler.pipeline.{ ConsolePipeline, Pipeline }
import io.github.wtog.crawler.selector.HtmlParser

import scala.concurrent.Future

/**
  * @author : tong.wang
  * @since : 5/16/18 9:48 PM
  * @version : 1.0.0
  */
trait PageProcessor extends HtmlParser {

  val name: String = this.getClass.getSimpleName

  /**
    * download client
    */
  val downloader: Downloader[_] = AsyncHttpClientDownloader

  /**
    * the target urls for processor to crawl
    *
    * @return
    */
  @deprecated
  def targetUrls: List[String] = Nil

  /**
    * the target request for processor to crawl
    * @return
    */
  def targetRequests: List[RequestUri] =
    if (targetUrls.nonEmpty) {
      targetUrls.map(url => RequestUri(url))
    } else {
      List.empty[RequestUri]
    }

  /**
    * handle the crawled result
    *
    * @return
    */
  val pipelines: Set[Pipeline] = Set(ConsolePipeline)

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
