package io.github.wtog.downloader

import akka.actor.{ Actor, Props }
import io.github.wtog.dto.{ DownloadEvent, ProcessorEvent }
import io.github.wtog.processor.PageProcessorActorReceiver
import org.slf4j.{ Logger, LoggerFactory }

/**
  * @author : tong.wang
  * @since : 5/16/18 11:54 PM
  * @version : 1.0.0
  */
class DownloaderActorReceiver extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DownloaderActorReceiver])

  lazy val processorActor = context.actorOf(
    Props[PageProcessorActorReceiver].withDispatcher("web-crawler.processor-dispatcher"),
    "page-processor"
  )

  override def receive: Receive = {
    case downloadEvent: DownloadEvent ⇒
      val spider = downloadEvent.spider

      import io.github.wtog.actor.ExecutionContexts.downloadDispatcher
      spider.downloader.download(spider, downloadEvent.request).foreach {
        case page if page.isDownloadSuccess ⇒
          processorActor ! ProcessorEvent(spider, page)
        case _ =>
      }
    case other ⇒
      logger.warn(s"${self.path} reviced wrong msg ${other}")
  }

  override def postStop(): Unit =
    if (logger.isWarnEnabled())
      logger.warn(s"downloader-processor [${self.path}] stoped!")

  override def postRestart(reason: Throwable): Unit =
    if (logger.isWarnEnabled())
      logger.warn(s"downloader-processor restart! ${reason.getLocalizedMessage}")
}
