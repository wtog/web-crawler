package io.github.wtog.processor

import java.util.concurrent.LinkedBlockingQueue

import akka.actor.{ Actor, ActorRef, Props }
import io.github.wtog.actor.ExecutionContexts.processorDispatcher
import io.github.wtog.dto.{ DownloadEvent, PipelineEvent, ProcessorEvent }
import io.github.wtog.pipeline.{ Pipeline, PipelineActorReceiver }
import io.github.wtog.queue.RequestQueue
import io.github.wtog.spider.Spider
import org.slf4j.{ Logger, LoggerFactory }

import scala.util.{ Failure, Success }

/**
  * @author : tong.wang
  * @since : 5/16/18 11:54 PM
  * @version : 1.0.0
  */
class PageProcessorActorReceiver extends Actor {

  private lazy val logger: Logger = LoggerFactory.getLogger(classOf[PageProcessorActorReceiver])

  val pipelineActor = context.actorOf(Props[PipelineActorReceiver].withDispatcher("web-crawler.pipeline-dispatcher"), "pipeline-processor")

  override def receive: Receive = {
    case processorEvent: ProcessorEvent ⇒
      val page           = processorEvent.page
      val spider         = processorEvent.spider
      val downloadSender = sender()

      spider.pageProcessor.process(page).onComplete {
        case Success(_) ⇒
          pipelineProcess(page.requestSetting.url.get, page.resultItems)(
            spider.pageProcessor.pipelines
          )
          spider.CrawlMetric.processedSuccessCounter

          continueRequest(page.requestQueue)(spider)(downloadSender)
        case Failure(value) ⇒
          logger.error(
            s"failed to process page ${page.url}",
            value
          )
      }
    case other ⇒
      logger.warn(s"${self.path} reviced wrong msg ${other}")
  }

  private[this] def pipelineProcess(url: String, pageResultItems: LinkedBlockingQueue[Any])(pipelines: Set[Pipeline]): Unit =
    while (!pageResultItems.isEmpty) {
      Option(pageResultItems.poll()).foreach { item ⇒
        pipelineActor ! PipelineEvent(pipelines, (url, item))
      }
    }

  private[this] def continueRequest(targetRequests: RequestQueue)(spider: Spider)(downloadSender: ActorRef): Unit =
    while (targetRequests.nonEmpty) {
      targetRequests.poll().foreach { targetRequest ⇒
        downloadSender ! DownloadEvent(spider, targetRequest)
      }
    }
}
