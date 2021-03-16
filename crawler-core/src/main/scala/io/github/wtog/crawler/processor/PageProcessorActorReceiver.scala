package io.github.wtog.crawler.processor

import java.util.concurrent.LinkedBlockingQueue

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import io.github.wtog.crawler.actor.ExecutionContexts.processorDispatcher
import io.github.wtog.crawler.dto.{ DownloadEvent, PipelineEvent, ProcessorEvent }
import io.github.wtog.crawler.pipeline.{ Pipeline, PipelineActorReceiver }
import io.github.wtog.crawler.queue.RequestQueue
import io.github.wtog.crawler.spider.Spider
import org.slf4j.{ Logger, LoggerFactory }

import scala.util.{ Failure, Success }

/**
  * @author : tong.wang
  * @since : 5/16/18 11:54 PM
  * @version : 1.0.0
  */
class PageProcessorActorReceiver extends Actor {

  private lazy val logger: Logger = LoggerFactory.getLogger(classOf[PageProcessorActorReceiver])

  val pipelineActor: ActorRef = context.actorOf(Props[PipelineActorReceiver].withDispatcher("crawler.pipeline-dispatcher"), "pipeline-processor")

  override def receive: Receive = {
    case processorEvent: ProcessorEvent ⇒
      val page           = processorEvent.page
      val spider         = processorEvent.spider
      val downloadSender = sender()

      spider.pageProcessor.process(page).foreach { _ ⇒
          pipelineProcess(page.requestSetting.url.get, page.resultItems)(spider.pageProcessor.pipelines)(downloadSender)
          spider.CrawlMetric.processedSuccessCounter

          continueRequest(page.requestQueue)(spider)(downloadSender)
      }
    case other ⇒
      logger.warn(s"${self.path} received wrong msg ${other}")
  }

  private[this] def pipelineProcess(url: String, pageResultItems: LinkedBlockingQueue[Any])(pipelines: Set[Pipeline])(downloadSender: ActorRef): Unit =
    while (!pageResultItems.isEmpty) {
      Option(pageResultItems.poll()).foreach { item ⇒
        PipelineEvent(pipelines, (url, item)).initPipelines() match {
          case Some(e) => pipelineActor ! e
          case None    => downloadSender ! PoisonPill
        }
      }
    }

  private[this] def continueRequest(targetRequests: RequestQueue)(spider: Spider)(downloadSender: ActorRef): Unit =
    while (targetRequests.nonEmpty) {
      targetRequests.poll().foreach { targetRequest ⇒
        downloadSender ! DownloadEvent(spider, targetRequest)
      }
    }
}
