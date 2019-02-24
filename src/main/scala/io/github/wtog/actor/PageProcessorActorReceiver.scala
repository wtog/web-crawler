package io.github.wtog.actor

import java.util.concurrent.{ LinkedBlockingQueue, TimeUnit }

import akka.actor.{ Actor, ActorRef }
import io.github.wtog.actor.ExecutionContexts.processorDispatcher
import io.github.wtog.pipeline.Pipeline
import io.github.wtog.processor.Page
import io.github.wtog.queue.RequestQueue
import io.github.wtog.spider.Spider
import org.slf4j.{ Logger, LoggerFactory }

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
      val page = processorEvent.page
      val spider = processorEvent.spider
      val downloadSender = sender()

      Future {
        spider.pageProcessor.process(page)
      } onComplete {
        case Success(_) ⇒
          pipelineProcess(page.requestSetting.url.get, page.resultItems)(spider.pageProcessor.pipelines)
          spider.CrawlMetric.processedSuccessCounter

          continueRequest(page.requestQueue)(spider)(downloadSender)
        case Failure(value) ⇒
          logger.error(s"failed to process page ${page.url}, cause ${value.getLocalizedMessage}")
          spider.CrawlMetric.processedFailedCounter
      }

    case other ⇒
      logger.warn(s"${self.path} reviced wrong msg ${other}")
  }

  private[this] def pipelineProcess(url: String, pageResultItems: LinkedBlockingQueue[Map[String, Any]])(pipelines: Set[Pipeline]): Unit = {
    while (!pageResultItems.isEmpty) {
      Option(pageResultItems.take()) foreach { items ⇒
        ActorManager.pipelineActor ! PipelineEvent(pipelines, (url, items))
      }
    }
  }

  private[this] def continueRequest(targetRequests: RequestQueue)(spider: Spider)(downloadSender: ActorRef) = {
    while (targetRequests.nonEmpty) {
      targetRequests.take().foreach { targetRequest ⇒
        TimeUnit.MILLISECONDS.sleep(targetRequest.sleepTime.toMillis)
        logger.info(s"${targetRequest.url.get} - ${targetRequest.sleepTime}")
        downloadSender ! DownloadEvent(spider, targetRequest)
      }
    }
  }
}

case class ProcessorEvent(spider: Spider, page: Page)
