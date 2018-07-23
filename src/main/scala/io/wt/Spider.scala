package io.wt

import java.util.concurrent.atomic.AtomicInteger

import org.slf4j.{Logger, LoggerFactory}
import io.wt.actor.{ActorManager, DownloadEvent}
import io.wt.downloader._
import io.wt.processor.{PageProcessor, RequestHeaderGeneral}

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
    execute()
  }

  private def execute: () => Unit = () => {
    this.pageProcessor.targetUrls.foreach(it => {
      ActorManager.downloaderActor ! DownloadEvent(this, Some(RequestHeaderGeneral(url = Some(it))))
    })
  }
}