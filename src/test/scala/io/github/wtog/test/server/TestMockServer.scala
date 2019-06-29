package io.github.wtog.test.server

import io.github.wtog.rest.{Router, Server}
import io.github.wtog.selector.HtmlParser
import io.netty.handler.codec.http.FullHttpRequest

import scala.collection.JavaConverters._

/**
  * @author : tong.wang
  * @since : 2019-08-01 09:10
  * @version : 1.0.0
  */
object TestMockServer {
  def start = Server.start(Set(TestMockRoute))
}

object TestMockRoute extends Router {
  override def method: String = "GET"

  override def route: String = s"/mock/list"

  override def handleRequest(request: FullHttpRequest): Array[Byte] = {
    val resp = request.headers().entries().asScala.foldLeft(Map.empty[String, String]){ (map, entry) =>
      map ++ Map(entry.getKey -> entry.getValue)
    }

    HtmlParser.toJson(resp).getBytes()
  }
}
