package wt

import org.slf4j.{Logger, LoggerFactory}
import wt.actor.ActorManager
import wt.downloader.{AsyncHttpClientDownloader, DownloadEvent, Downloader, RequestHeaderGeneral}
import wt.pipeline.{ConsolePipeline, Pipeline}
import wt.processor.PageProcessor
import wt.queue.{LinkQueue, RequestQueue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author : tong.wang
  * @since : 4/10/18 11:34 AM
  * @version : 1.0.0
  */
case class Spider(
  urls: List[String],
  pipelineList: List[Pipeline] = List(ConsolePipeline()),
  pageProcessor: PageProcessor,
  downloader: Downloader = AsyncHttpClientDownloader,
  targetUrls: RequestQueue = new LinkQueue()) {

  val logger: Logger = LoggerFactory.getLogger(Spider.getClass)

  def start() = {
    Future {
      execute()
    }
  }

  def startSync() = {
    execute()
  }

  private def initRequest(): Unit = {
    this.urls.foreach(it => {
      this.targetUrls.push(RequestHeaderGeneral(url = Some(it)))
    })
  }

  private def execute: () => Unit = () => {
    initRequest()

    var flag = true
    while (flag) {
      targetUrls.poll() match {
        case r @ Some(request) =>
          ActorManager.downloaderActor ! DownloadEvent(this, r)
        case None =>
          flag = targetUrls.isEmpty
      }
    }
  }
}