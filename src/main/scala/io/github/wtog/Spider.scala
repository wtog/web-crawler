package io.github.wtog

import java.util.concurrent.atomic.AtomicInteger

import io.github.wtog.actor.{ ActorManager, DownloadEvent }
import io.github.wtog.downloader._
import io.github.wtog.processor.{ PageProcessor, RequestHeaderGeneral }
import org.slf4j.{ Logger, LoggerFactory }

/**
 * @author : tong.wang
 * @since : 4/10/18 11:34 AM
 * @version : 1.0.0
 */
case class Spider(
    pageProcessor: PageProcessor,
    downloader:    Downloader    = ApacheHttpClientDownloader) {

  val logger: Logger = LoggerFactory.getLogger(Spider.getClass)

  private val downloadPageSuccessedSize: AtomicInteger = new AtomicInteger(0)
  private val downloadPageFailedSize: AtomicInteger = new AtomicInteger(0)

  val downloadPageSize: Int = downloadPageSuccessedSize.get() + downloadPageFailedSize.get()

  def downloadSuccessSum() = downloadPageSuccessedSize.getAndIncrement()

  def downloadFailedSum() = downloadPageFailedSize.getAndIncrement()

  def start() = {
    execute()
  }

  private def execute: () ⇒ Unit = () ⇒ {
    this.pageProcessor.targetUrls.foreach(it ⇒ {
      ActorManager.downloaderActor ! DownloadEvent(this, Some(RequestHeaderGeneral(url = Some(it))))
    })
  }
}
