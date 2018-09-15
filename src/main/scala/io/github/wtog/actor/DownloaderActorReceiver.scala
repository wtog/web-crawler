package io.github.wtog.actor

import akka.actor.{ Actor, Props }
import io.github.wtog.processor.RequestHeaderGeneral
import io.github.wtog.spider.Spider
import org.slf4j.{ Logger, LoggerFactory }

import scala.util.{ Failure, Success }

/**
 * @author : tong.wang
 * @since : 5/16/18 11:54 PM
 * @version : 1.0.0
 */
class DownloaderActorRevicer extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DownloaderActorRevicer])

  lazy val processorActor = context.actorOf(Props[PageProcessorActorRevicer].withDispatcher("processor-dispatcher"), "page-processor")

  override def receive: Receive = {
    case downloadEvent: DownloadEvent ⇒
      downloadEvent.request.foreach(request ⇒ {
        val spider = downloadEvent.spider

        import io.github.wtog.actor.ExecutionContexts.downloadDispatcher

        spider.downloader.download(spider.pageProcessor.requestHeaders.copy(requestHeaderGeneral = Some(request))) onComplete {
          case Success(page) ⇒
            if (page.isDownloadSuccess) {
              spider.CrawlMetric.downloadSuccessCounter
              processorActor ! ProcessorEvent(downloadEvent.spider, page)
            } else {
              logger.warn("failed to download")
              spider.CrawlMetric.downloadFailedCounter
            }
          case Failure(e) ⇒
            spider.CrawlMetric.downloadFailedCounter
            throw e
        }
      })
    case other ⇒ logger.warn(s"${self.path} reviced wrong msg ${other}")
  }

  override def postStop(): Unit = {
    if (logger.isWarnEnabled()) logger.warn(s"downloader-processor [${self.path}] stoped!")
  }

  override def postRestart(reason: Throwable): Unit = {
    if (logger.isWarnEnabled()) logger.warn("downloader-processor restart!")
  }
}

case class DownloadEvent(spider: Spider, request: Option[RequestHeaderGeneral])
