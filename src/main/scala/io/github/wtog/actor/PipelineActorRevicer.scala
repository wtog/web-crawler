package io.github.wtog.actor

import akka.actor.Actor
import org.slf4j.{ Logger, LoggerFactory }
import io.github.wtog.pipeline.Pipeline

import scala.concurrent.Future

/**
 * @author : tong.wang
 * @since : 5/16/18 11:54 PM
 * @version : 1.0.0
 */
class PipelineActorRevicer extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[PipelineActorRevicer])

  override def receive: Receive = {
    case pipelineEvent: PipelineEvent ⇒
      import ExecutionContexts.pipelineDispatcher

      pipelineEvent.pipelineList.foreach(it ⇒ Future {
        it.process(pipelineEvent.pageResultItems)
      })
    case other ⇒ logger.warn(s"${this.getClass.getSimpleName} reviced wrong msg ${other}")
  }
}

case class PipelineEvent(pipelineList: Set[Pipeline], pageResultItems: (String, Map[String, Any]))
