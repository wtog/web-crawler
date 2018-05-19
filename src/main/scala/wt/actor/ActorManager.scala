package wt.actor

import akka.actor.{ActorSystem, Props}

import scala.concurrent.ExecutionContext

/**
  * @author : tong.wang
  * @since : 5/16/18 11:56 PM
  * @version : 1.0.0
  */
object ActorManager {
  lazy val system = ActorSystem("web-crawler")

  lazy val processorActor = system.actorOf(Props[PageProcessorActorRevicer].withDispatcher("processor-dispatcher"), "page-processor")
  lazy val downloaderActor = system.actorOf(Props[DownloaderActorRevicer].withDispatcher("downloader-dispatcher"), "downloader-processor")
  lazy val pipelineActor = system.actorOf(Props[PipelineActorRevicer].withDispatcher("pipeline-dispatcher"), "pipeline-processor")
}

class ActorManager{}

object ExecutionContexts {
  implicit lazy val downloadDispatcher: ExecutionContext = ActorManager.system.dispatchers.lookup("downloader-dispatcher")
  implicit lazy val processorDispatcher: ExecutionContext = ActorManager.system.dispatchers.lookup("processor-dispatcher")
  implicit lazy val pipelineDispatcher: ExecutionContext = ActorManager.system.dispatchers.lookup("pipeline-dispatcher")
}
