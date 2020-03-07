package io.github.wtog.crawler.actor

import akka.actor.{ActorSystem, Props}

import scala.concurrent.ExecutionContext
import akka.actor.{ActorRef, ActorSelection}
import akka.dispatch.MessageDispatcher

/**
  * @author : tong.wang
  * @since : 5/16/18 11:56 PM
  * @version : 1.0.0
  */
object ActorManager {
  lazy val system: ActorSystem = ActorSystem("crawler")

  def getNewSystemActor(dispatcher: String, actorName: String, props: Props): ActorRef = system.actorOf(props.withDispatcher(s"crawler.${dispatcher}"), actorName)

  def getExistedActor(path: String): ActorSelection = system.actorSelection(path)
}

object ExecutionContexts {
  implicit lazy val downloadDispatcher: ExecutionContext  = dispatcher("crawler.downloader-dispatcher")
  implicit lazy val processorDispatcher: ExecutionContext = dispatcher("crawler.processor-dispatcher")
  implicit lazy val pipelineDispatcher: ExecutionContext  = dispatcher("crawler.pipeline-dispatcher")

  def dispatcher(id: String): MessageDispatcher = {
    val dispatchers = ActorManager.system.dispatchers
    if (dispatchers.hasDispatcher(id)) {
      dispatchers.lookup(id)
    } else {
      dispatchers.defaultGlobalDispatcher
    }
  }
}
