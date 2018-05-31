package wt.actor

import akka.actor.Actor
import org.slf4j.{Logger, LoggerFactory}
import wt.downloader.DownloadEvent

import scala.util.{Failure, Success}

/**
  * @author : tong.wang
  * @since : 5/16/18 11:54 PM
  * @version : 1.0.0
  */
class DownloaderActorRevicer extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DownloaderActorRevicer])

  override def receive: Receive = {
    case downloadEvent: DownloadEvent =>
      downloadEvent.request.foreach(request => {
        val spider = downloadEvent.spider

        import wt.actor.ExecutionContexts.downloadDispatcher

        spider.downloader.download(spider.pageProcessor.requestHeaders.copy(requestHeaderGeneral = Some(request))) onComplete {
          case Success(page) =>
            if (page.isDownloadSuccess) {
              spider.downloadSuccessSum()
              ActorManager.processorActor ! ProcessorEvent(downloadEvent.spider, page)
            } else {
              logger.warn("failed to download")
              spider.downloadFailedSum()
            }
          case Failure(e) =>
            spider.downloadFailedSum()
            throw e
        }
      })

    case other => logger.warn(s"${this.getClass.getSimpleName} reviced wrong msg ${other}")
  }
}

