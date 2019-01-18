package io.github.wtog.actor

import java.util.concurrent.LinkedBlockingQueue

import akka.actor.{ Actor, ActorRef }
import org.slf4j.{ Logger, LoggerFactory }
import io.github.wtog.pipeline.Pipeline
import io.github.wtog.processor.Page
import io.github.wtog.queue.RequestQueue
import io.github.wtog.spider.Spider
import ExecutionContexts.processorDispatcher

import scala.concurrent.Future
import scala.util.{ Failure, Success }

/**
 * @author : tong.wang
 * @since : 5/16/18 11:54 PM
 * @version : 1.0.0
 */
class PageProcessorActorRevicer extends Actor {

  private lazy val logger: Logger = LoggerFactory.getLogger(classOf[PageProcessorActorRevicer])

  override def receive: Receive = {
    case processorEvent: ProcessorEvent ⇒
      val downloadSender = sender()
      val page = processorEvent.page
      val spider = processorEvent.spider

      Future {
        spider.pageProcessor.process(page)
      } onComplete {
        case Success(_) ⇒
          continueAddRequest(page.requestQueue)(spider)(downloadSender)
          addToPipeline(page.requestGeneral.url.get, page.resultItems)(spider.pageProcessor.pipelines)
          spider.CrawlMetric.processedSuccessCounter
        case Failure(value) ⇒
          logger.error(s"failed to process page ${page.url}, cause ${value.getLocalizedMessage}")
          spider.CrawlMetric.processedFailedCounter
      }

    case other ⇒
      logger.warn(s"${self.path} reviced wrong msg ${other}")
  }

  def addToPipeline(url: String, pageResultItems: LinkedBlockingQueue[Map[String, Any]])(pipelines: Set[Pipeline]): Unit = {
    while (!pageResultItems.isEmpty) {
      Option(pageResultItems.poll()) foreach {
        items ⇒ ActorManager.pipelineActor ! PipelineEvent(pipelines, (url, items))
      }
    }
  }

  def continueAddRequest(targetRequests: RequestQueue)(spider: Spider)(downloadSender: ActorRef) = {
    while (!targetRequests.isEmpty) {
      import scala.concurrent.duration._
      val newDownloadTask = DownloadEvent(spider, targetRequests.poll())
      ActorManager.system.scheduler.scheduleOnce(spider.pageProcessor.requestHeaders.sleepTime millisecond)(downloadSender ! newDownloadTask)
    }
  }
}

case class ProcessorEvent(spider: Spider, page: Page)
