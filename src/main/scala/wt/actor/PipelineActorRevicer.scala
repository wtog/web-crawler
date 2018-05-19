package wt.actor

import akka.actor.Actor
import org.slf4j.{Logger, LoggerFactory}
import wt.Spider

/**
  * @author : tong.wang
  * @since : 5/16/18 11:54 PM
  * @version : 1.0.0
  */
class PipelineActorRevicer extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[PipelineActorRevicer])

  override def receive: Receive = {
    case pipelineEvent: PipelineEvent =>
      val spider = pipelineEvent.spider
      val pageResultItem = pipelineEvent.pageResultItems
      spider.pipelineList.foreach(_.process(pageResultItem))
    case other => logger.warn(s"${this.getClass.getSimpleName} reviced wrong msg ${other}")
  }
}

case class PipelineEvent(spider: Spider, pageResultItems: (String, Any))