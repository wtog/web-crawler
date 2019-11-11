package io.github.wtog.crawler.pipeline

import akka.actor.Actor
import io.github.wtog.crawler.dto.PipelineEvent
import org.slf4j.{ Logger, LoggerFactory }

/**
  * @author : tong.wang
  * @since : 5/16/18 11:54 PM
  * @version : 1.0.0
  */
class PipelineActorReceiver extends Actor {

  private lazy val logger: Logger = LoggerFactory.getLogger(classOf[PipelineActorReceiver])

  override def receive: Receive = {
    case pipelineEvent: PipelineEvent[_] ⇒
      val pipelineList = if (logger.isTraceEnabled()) {
        pipelineEvent.pipelineList + ConsolePipeline
      } else {
        pipelineEvent.pipelineList
      }

      pipelineList.foreach { _.process(pipelineEvent.pageResultItems) }
    case other ⇒
      logger.warn(s"${this.getClass.getSimpleName} reviced wrong msg ${other}")
  }
}
