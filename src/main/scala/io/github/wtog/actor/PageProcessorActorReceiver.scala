package io.github.wtog.actor

import java.util.concurrent.LinkedBlockingQueue

import akka.actor.Actor
import org.slf4j.{ Logger, LoggerFactory }
import io.github.wtog.pipeline.Pipeline
import io.github.wtog.processor.Page
import io.github.wtog.queue.RequestQueue
import io.github.wtog.spider.Spider

import scala.concurrent.Future
import scala.util.{ Failure, Success }

/**
 * @author : tong.wang
 * @since : 5/16/18 11:54 PM
 * @version : 1.0.0
 */
class PageProcessorActorRevicer extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[PageProcessorActorRevicer])

  override def receive: Receive = {
    case processorEvent: ProcessorEvent ⇒
      val page = processorEvent.page
      val spider = processorEvent.spider

      import ExecutionContexts.processorDispatcher

      Future {
        spider.pageProcessor.process(page)
      } onComplete {
        case Success(_) ⇒
          continueAddRequest(page.requestQueue)(spider)
          addToPipeline(page.requestGeneral.url.get, page.resultItems)(spider.pageProcessor.pipelines)
          spider.CrawlMetric.processedSuccessCounter
        case Failure(value) ⇒
          logger.error(s"failed to process page, cause ${value.getLocalizedMessage}")
          spider.CrawlMetric.processedFailedCounter
      }

    case other ⇒ logger.warn(s"${this.getClass.getSimpleName} reviced wrong msg ${other}")
  }

  def addToPipeline(url: String, pageResultItems: (LinkedBlockingQueue[Map[String, Any]], Int))(pipelines: Set[Pipeline]): Unit = {
    val (resultItems, batchPollSize) = pageResultItems
    if (!resultItems.isEmpty) {
      (0 to batchPollSize).foreach(_ ⇒ {
        Option(resultItems.poll()) foreach {
          items ⇒ ActorManager.pipelineActor ! PipelineEvent(pipelines, (url, items))
        }
      })
    }
  }

  def continueAddRequest(targetRequests: (RequestQueue, Int))(spider: Spider) = {
    val (requestQueue, batchPollSize) = targetRequests
    if (!requestQueue.isEmpty) {
      (0 to batchPollSize).foreach(_ ⇒ {
        import scala.concurrent.ExecutionContext.Implicits.global
        import scala.concurrent.duration._
        ActorManager.system.scheduler.scheduleOnce(spider.pageProcessor.requestHeaders.sleepTime millisecond)(spider.downloaderActor ! DownloadEvent(spider, requestQueue.poll()))
      })
    }

  }
}

case class ProcessorEvent(spider: Spider, page: Page)
