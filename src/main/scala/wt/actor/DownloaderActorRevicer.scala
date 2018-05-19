package wt.actor

import akka.actor.Actor
import org.slf4j.{Logger, LoggerFactory}
import wt.downloader.DownloadEvent

/**
  * @author : tong.wang
  * @since : 5/16/18 11:54 PM
  * @version : 1.0.0
  */
class DownloaderActorRevicer extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DownloaderActorRevicer])

  override def receive: Receive = {
    case downloadEvent: DownloadEvent =>
      downloadEvent.request match {
        case Some(request) =>
          val spider = downloadEvent.spider

          val page = spider.downloader.download(spider.pageProcessor.requestHeaders.copy(requestHeaderGeneral = Some(request)))

          if (page.isDownloadSuccess) {
            ActorManager.processorActor ! ProcessorEvent(downloadEvent.spider, page)

            if (logger.isDebugEnabled()) {
              logger.debug("send page")
            }
          } else {
            logger.warn("failed to download")
          }
        case None => {}
      }
    case other => logger.warn(s"${this.getClass.getSimpleName} reviced wrong msg ${other}")
  }
}

