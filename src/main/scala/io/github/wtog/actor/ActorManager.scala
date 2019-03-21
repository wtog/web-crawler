package io.github.wtog.actor

import akka.actor.{ ActorSystem, Props }

import scala.concurrent.ExecutionContext

/**
  * @author : tong.wang
  * @since : 5/16/18 11:56 PM
  * @version : 1.0.0
  */
object ActorManager {
  lazy val system = ActorSystem("web-crawler")

  def getNewSystemActor(dispatcher: String, actorName: String, props: Props) = system.actorOf(props.withDispatcher(s"web-crawler.${dispatcher}"), actorName)

  def getExistedAcotr(path: String) = system.actorSelection(path)
}

object ExecutionContexts {
  implicit lazy val downloadDispatcher: ExecutionContext  = ActorManager.system.dispatchers.lookup("web-crawler.downloader-dispatcher")
  implicit lazy val processorDispatcher: ExecutionContext = ActorManager.system.dispatchers.lookup("web-crawler.processor-dispatcher")
  implicit lazy val pipelineDispatcher: ExecutionContext  = ActorManager.system.dispatchers.lookup("web-crawler.pipeline-dispatcher")
}
