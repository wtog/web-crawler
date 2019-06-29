package io.github.wtog.rest

import java.util.concurrent.Executors

import io.github.wtog.utils.ConfigUtils
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
  * @author : tong.wang
  * @since : 2019-08-28 10:24
  * @version : 1.0.0
  */
trait Server extends Logging {

  @volatile var running = false

  def start(routes: Set[Router]): Boolean = {
    if (!running) {
      Future {
        running = true
        try {
          doStart(routes)
        } catch {
          case NonFatal(e) =>
            logger.error(e.getLocalizedMessage)
            running = false
        }
      }(ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor()))
    } else {
      running = true
    }
    running
  }

  protected def doStart(routes: Set[Router])

  val defaultRoutes: Set[Router] = Set(SpiderStatusRoute)

  val port = ConfigUtils.getIntOpt("server.port").getOrElse(19000)

}

object Server {
  val serverInstance = NettyServer

  def start(routes: Set[Router] = Set.empty[Router]): Boolean = serverInstance.start(routes)

  def running: Boolean = serverInstance.running
}
