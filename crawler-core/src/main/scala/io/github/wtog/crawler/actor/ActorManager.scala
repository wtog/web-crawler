package io.github.wtog.crawler.actor

import akka.actor.{ ActorSystem, Props }

import scala.concurrent.ExecutionContext
import akka.actor.{ ActorRef, ActorSelection }

/**
  * @author : tong.wang
  * @since : 5/16/18 11:56 PM
  * @version : 1.0.0
  */
object ActorManager {
  lazy val system: ActorSystem = ActorSystem("crawler")

  def getNewSystemActor(dispatcher: String, actorName: String, props: Props): ActorRef = system.actorOf(props.withDispatcher(s"crawler.${dispatcher}"), actorName)

  def getExistedAcotr(path: String): ActorSelection = system.actorSelection(path)
}

object ExecutionContexts {
  implicit lazy val downloadDispatcher: ExecutionContext  = ActorManager.system.dispatchers.lookup("crawler.downloader-dispatcher")
  implicit lazy val processorDispatcher: ExecutionContext = ActorManager.system.dispatchers.lookup("crawler.processor-dispatcher")
  implicit lazy val pipelineDispatcher: ExecutionContext  = ActorManager.system.dispatchers.lookup("crawler.pipeline-dispatcher")
}
