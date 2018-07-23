package io.wt

import java.io.PrintStream
import java.net.{InetAddress, ServerSocket, Socket}
import java.util.concurrent.TimeUnit

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.BufferedSource
import scala.util.Success

/**
  * @author : tong.wang
  * @since : 6/23/18 12:21 AM
  * @version : 1.0.0
  */
class SparkSpec extends BaseTest {

  "tcp " should "send to spark" in {
    val server = new ServerSocket(8066)

    var clients: Map[String, Socket] = Map()
    
      val s = server.accept()
      clients += (s.getInetAddress.getHostName -> s)


    while (true) {
      if (clients.nonEmpty) {
        clients.foreach { case(clientId, s) =>
          val out = new PrintStream(s.getOutputStream)

          out.println(s"hh clientId ${System.nanoTime()}")
          out.flush()
        }

      } else {
        println("non clients connected! ")
      }
      TimeUnit.SECONDS.sleep(1)
    }



  }

}
