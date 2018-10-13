package io.github.wtog.spider

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ Cancellable, PoisonPill }
import io.github.wtog.actor.{ ActorManager, DownloadEvent }
import io.github.wtog.downloader.proxy.crawler.ProxyProcessorTrait
import io.github.wtog.downloader.{ ApacheHttpClientDownloader, Downloader }
import io.github.wtog.processor.{ PageProcessor, RequestHeaderGeneral }
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

  lazy val logger = LoggerFactory.getLogger(classOf[Spider])

  private var metircInfoCron: Option[Cancellable] = None
  var downloaderActor = ActorManager.createActor("downloader-dispatcher", s"downloader-${name}")

  def start(): Unit = {
    execute()
    metircInfoCron = Option(ActorManager.system.scheduler.schedule(2 seconds, 1 seconds)(CrawlMetric.metricInfo()))
    SpiderPool.addSpider(this)
  }

  def restart(): Unit = {
    downloaderActor = ActorManager.createActor("downloader-dispatcher", s"downloader-${name}-${System.currentTimeMillis()}")
    start()
  }

  def stop() = {
    downloaderActor ! PoisonPill
    metircInfoCron.foreach { c ⇒
      c.cancel()
      this.CrawlMetric.clean()
      SpiderPool.removeSpider(this)
    }
  }

  private def execute: () ⇒ Unit = () ⇒ {
    this.pageProcessor.targetUrls.foreach(it ⇒ {
      downloaderActor ! DownloadEvent(this, Some(RequestHeaderGeneral(url = Some(it))))
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
        logger.info(s"[${name}-spider] downloaded<->processed: ${downloadedPageSum}<->${processedPageSum}, sc: ${downloadPageSuccessNum.get()}<->${processPageSuccessNum}, fc: ${downloadPageFailedNum.get()}<->${processPageFailedNum.get()},")
    }

  }

  override def toString: String = s"spider-${name}: ${CrawlMetric.downloadedPageSum}"
}
