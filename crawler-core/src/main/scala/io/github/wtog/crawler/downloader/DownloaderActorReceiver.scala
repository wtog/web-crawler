package io.github.wtog.crawler.downloader

import akka.actor.{ Actor, Props }
import io.github.wtog.crawler.dto.{ DownloadEvent, ProcessorEvent }
import io.github.wtog.crawler.processor.PageProcessorActorReceiver
import org.slf4j.{ Logger, LoggerFactory }
import akka.actor.ActorRef

/**
  * @author : tong.wang
  * @since : 5/16/18 11:54 PM
  * @version : 1.0.0
  */
class DownloaderActorReceiver extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DownloaderActorReceiver])

  lazy val processorActor: ActorRef = context.actorOf(
    Props[PageProcessorActorReceiver].withDispatcher("crawler.processor-dispatcher"),
    "page-processor"
  )

  override def receive: Receive = {
    case downloadEvent: DownloadEvent ⇒
      val spider = downloadEvent.spider

      import io.github.wtog.crawler.actor.ExecutionContexts.downloadDispatcher
      spider.downloader.download(spider, downloadEvent.request).foreach {
        case page if page.isDownloadSuccess ⇒
          processorActor ! ProcessorEvent(spider, page)
        case page =>
          logger.warn(s"page failed to download cause ${page.source}")
      }
    case other ⇒
      logger.warn(s"${self.path} received wrong msg ${other}")
  }

  override def postStop(): Unit =
    if (logger.isWarnEnabled())
      logger.warn(s"downloader-processor [${self.path}] stopped!")

  override def postRestart(reason: Throwable): Unit =
    if (logger.isWarnEnabled())
      logger.warn(s"downloader-processor restart! ${reason.getLocalizedMessage}")
}
