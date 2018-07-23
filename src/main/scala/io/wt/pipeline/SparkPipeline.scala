package io.wt.pipeline

import java.io.PrintStream
import java.net.{ServerSocket, Socket}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import io.wt.pipeline.SparkPipeline.server

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author : tong.wang
  * @since : 6/22/18 7:58 AM
  * @version : 1.0.0
  */
object SparkPipeline extends Pipeline {

  val server = new ServerSocket(8066)
  var clients: Map[String, Socket] = Map()
  val builtServerSocket: AtomicBoolean = new AtomicBoolean(false)

  def buildSocket() = {
      val s = server.accept()
      clients += (s.getInetAddress.getHostName -> s)
  }

  override def process(pageResultItem: (String, Map[String, Any])): Unit = {
    if (builtServerSocket.getAndSet(true)) {
      buildSocket()
    }

    if (clients.nonEmpty) {
      clients.foreach { case (_, s) =>
        val out = new PrintStream(s.getOutputStream)
        val values = pageResultItem._2.values.mkString("|")
        println(values)
        out.println(values)
        out.flush()
//        out.close()
      }
    } else {
      println(s"non clients connected! ${clients}")
    }

  }
}

