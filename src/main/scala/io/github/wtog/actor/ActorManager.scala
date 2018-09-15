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

  lazy val pipelineActor = system.actorOf(Props[PipelineActorRevicer].withDispatcher("pipeline-dispatcher"), "pipeline-processor")

  def createActor(dispatcher: String, actorName: String) = {
    system.actorOf(Props[DownloaderActorRevicer].withDispatcher(dispatcher), actorName)
  }

}

class ActorManager {}

object ExecutionContexts {
  implicit lazy val downloadDispatcher: ExecutionContext = ActorManager.system.dispatchers.lookup("downloader-dispatcher")
  implicit lazy val processorDispatcher: ExecutionContext = ActorManager.system.dispatchers.lookup("processor-dispatcher")
  implicit lazy val pipelineDispatcher: ExecutionContext = ActorManager.system.dispatchers.lookup("pipeline-dispatcher")
}
