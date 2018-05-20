package wt.actor

import java.util.concurrent.LinkedBlockingQueue

import akka.actor.Actor
import org.slf4j.{Logger, LoggerFactory}
import wt.Spider
import wt.downloader.DownloadEvent
import wt.processor.Page
import wt.queue.RequestQueue

/**
  * @author : tong.wang
  * @since : 5/16/18 11:54 PM
  * @version : 1.0.0
  */
class PageProcessorActorRevicer extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[PageProcessorActorRevicer])

  override def receive: Receive = {
    case processorEvent: ProcessorEvent =>
      val page = processorEvent.page
      val spider = processorEvent.spider

      if (page.isDownloadSuccess) {
        spider.pageProcessor.process(page)
        
        continueAddRequest(page.requestQueue)(spider)
        addToPipeline(page.requestGeneral.url.get, page.resultItems)(spider)

        if (logger.isDebugEnabled()) {
          logger.debug("page processor")
        }
      } else {
        logger.warn("failed to download")
      }
    case other => logger.warn(s"${this.getClass.getSimpleName} reviced wrong msg ${other}")
  }

  def addToPipeline(url: String, pageResultItems: (LinkedBlockingQueue[Map[String, Any]], Int))(spider: Spider) = {
    pageResultItems match {
      case (resultItems, batchPollSize) => {
        while (!resultItems.isEmpty) {
          for (i <- 0 to batchPollSize) {
            Option(resultItems.poll()) match {
              case Some(items) => ActorManager.pipelineActor ! PipelineEvent(spider, (url, items))
              case None => {}
            }
          }
        }
      }
    }

  }

  def continueAddRequest(targetRequests: (RequestQueue, Int))(spider: Spider) = {
    targetRequests match {
      case (requestQueue, batchPollSize) => {
        while (!requestQueue.isEmpty) {
          for (i <- 0 to batchPollSize) {
            ActorManager.downloaderActor ! DownloadEvent(spider, requestQueue.poll())
          }
        }
      }
      case other => logger.warn(s"wrong targetRequests type ${other}")
    }
  }
}

case class ProcessorEvent(spider: Spider, page: Page)
