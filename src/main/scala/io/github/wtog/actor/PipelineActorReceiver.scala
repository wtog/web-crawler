package io.github.wtog.actor

import akka.actor.Actor
import io.github.wtog.pipeline.{ ConsolePipeline, Pipeline }
import org.slf4j.{ Logger, LoggerFactory }

/**
 * @author : tong.wang
 * @since : 5/16/18 11:54 PM
 * @version : 1.0.0
 */
class PipelineActorRevicer extends Actor {

  private lazy val logger: Logger = LoggerFactory.getLogger(classOf[PipelineActorRevicer])

  override def receive: Receive = {
    case pipelineEvent: PipelineEvent ⇒
      val pipelineList = if (logger.isDebugEnabled) {
        pipelineEvent.pipelineList + ConsolePipeline
      } else {
        pipelineEvent.pipelineList
      }

      pipelineList.foreach(_.process(pipelineEvent.pageResultItems))
    case other ⇒
      logger.warn(s"${this.getClass.getSimpleName} reviced wrong msg ${other}")
  }
}

case class PipelineEvent(pipelineList: Set[Pipeline], pageResultItems: (String, Map[String, Any]))
