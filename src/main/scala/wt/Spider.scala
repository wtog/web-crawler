package wt

import java.util.concurrent.atomic.AtomicInteger

import org.slf4j.{Logger, LoggerFactory}
import wt.actor.ActorManager
import wt.downloader._
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
  downloader: Downloader = ApacheHttpClientDownloader,
  targetUrls: RequestQueue = new LinkQueue()) {

  val logger: Logger = LoggerFactory.getLogger(Spider.getClass)

  private val downloadPageSuccessedSize: AtomicInteger = new AtomicInteger(0)
  private val downloadPageFailedSize: AtomicInteger = new AtomicInteger(0)

  val downloadPageSize: Int = downloadPageSuccessedSize.get() + downloadPageFailedSize.get()

  def downloadSuccessSum() = downloadPageSuccessedSize.getAndIncrement()
  def downloadFailedSum() = downloadPageFailedSize.getAndIncrement()

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
        case r @ Some(_) =>
          ActorManager.downloaderActor ! DownloadEvent(this, r)
        case None =>
          flag = targetUrls.isEmpty
      }
    }
  }
}