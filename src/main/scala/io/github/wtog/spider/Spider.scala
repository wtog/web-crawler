package io.github.wtog.spider

import java.util.concurrent.atomic.{ AtomicBoolean, AtomicInteger }

import akka.actor.{ ActorRef, Cancellable, PoisonPill, Props }
import io.github.wtog.actor.{ ActorManager, DownloadEvent, DownloaderActorRevicer }
import io.github.wtog.downloader.proxy.crawler.ProxyProcessorTrait
import io.github.wtog.downloader.{ ApacheHttpClientDownloader, Downloader }
import io.github.wtog.processor.PageProcessor
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._

/**
 * @author : tong.wang
 * @since : 4/10/18 11:34 AM
 * @version : 1.0.0
 */
case class Spider(
    name:          String        = Thread.currentThread().getName,
    pageProcessor: PageProcessor,
    downloader:    Downloader    = ApacheHttpClientDownloader) {

  private lazy val logger = LoggerFactory.getLogger(classOf[Spider])

  val running: AtomicBoolean = new AtomicBoolean(false)
  private var metircInfoCron: Option[Cancellable] = None
  private var downloaderActorPath = ""

  def start(): Unit = {
    if (!running.getAndSet(true)) {
      downloaderActorPath = s"downloader-${name}-${System.currentTimeMillis()}"
      val downloaderActor = ActorManager.getNewSystemActor("downloader-dispatcher", downloaderActorPath, props = Props[DownloaderActorRevicer])
      execute(downloaderActor)
      SpiderPool.addSpider(this)

      if (logger.isDebugEnabled()) {
        metircInfoCron = Option(ActorManager.system.scheduler.schedule(2 seconds, 10 seconds)(CrawlMetric.metricInfo()))
      }
    }
  }

  def restart(): Unit = {
    if (running.get()) {
      this.stop()
    }

    start()
  }

  def stop(): Unit = {
    if (running.getAndSet(false)) {
      ActorManager.getExistedAcotr(downloaderActorPath) ! PoisonPill
      SpiderPool.removeSpider(this)
      this.CrawlMetric.clean()
      metircInfoCron.foreach { _.cancel() }
    }
  }

  private def execute(downloaderActor: ActorRef): Unit = {
    this.pageProcessor.targetUrls.foreach(url ⇒ {
      downloaderActor ! DownloadEvent(this, request = pageProcessor.requestSetting.withUrl(url))
    })
  }

  object CrawlMetric {
    val downloadPageSuccessNum = new AtomicInteger(0)
    val downloadPageFailedNum = new AtomicInteger(0)
    val processPageSuccessNum = new AtomicInteger(0)
    val processPageFailedNum = new AtomicInteger(0)

    def downloadedPageSum = downloadPageSuccessNum.get() + downloadPageFailedNum.get()

    def processedPageSum = processPageSuccessNum.get() + processPageFailedNum.get()

    def downloadSuccessCounter = downloadPageSuccessNum.getAndIncrement()

    def downloadFailedCounter = downloadPageFailedNum.getAndIncrement()

    def processedSuccessCounter = processPageSuccessNum.getAndIncrement()

    def processedFailedCounter = processPageFailedNum.getAndIncrement()

    def clean() = {
      downloadPageSuccessNum.set(0)
      downloadPageFailedNum.set(0)
      processPageSuccessNum.set(0)
      processPageFailedNum.set(0)
    }

    def metricInfo() = {
      if (downloadedPageSum + processedPageSum > 0 && !pageProcessor.isInstanceOf[ProxyProcessorTrait])
        logger.debug(s"[${name}-spider] downloaded<->processed: ${downloadedPageSum}<->${processedPageSum}, sc: ${downloadPageSuccessNum.get()}<->${processPageSuccessNum}, fc: ${downloadPageFailedNum.get()}<->${processPageFailedNum.get()},")
    }

  }

  override def toString: String = s"spider-${name}: ${CrawlMetric.downloadedPageSum}"
}
