package wt

import java.util.concurrent.atomic.AtomicInteger

import org.slf4j.{Logger, LoggerFactory}
import wt.actor.{ActorManager, DownloadEvent}
import wt.downloader._
import wt.processor.PageProcessor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author : tong.wang
  * @since : 4/10/18 11:34 AM
  * @version : 1.0.0
  */
case class Spider(
  pageProcessor: PageProcessor,
  downloader: Downloader = ApacheHttpClientDownloader) {

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

  private def execute: () => Unit = () => {
    this.pageProcessor.targetUrls.foreach(it => {
      ActorManager.downloaderActor ! DownloadEvent(this, Some(RequestHeaderGeneral(url = Some(it))))
    })
  }
}