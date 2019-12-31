package io.github.wtog.crawler.test.server

import java.nio.charset.Charset

import io.github.wtog.crawler.rest.{Router, Server}
import io.github.wtog.utils.JsonUtils
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.{FullHttpRequest, HttpMethod}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

/**
  * @author : tong.wang
  * @since : 2019-08-01 09:10
  * @version : 1.0.0
  */
object TestMockServer {
  def start = Server.start(Set(TestGetMockRoute, TestPostMockRoute))
}

object TestGetMockRoute extends Router {
  override def method: String = HttpMethod.GET.toString

  override def route: String = s"/mock/get"

  override def handleRequest(request: FullHttpRequest): Array[Byte] = {
    val resp = request.headers().entries().asScala.foldLeft(Map.empty[String, String]){ (map, entry) =>
      map ++ Map(entry.getKey -> entry.getValue)
    }

    JsonUtils.toJson(resp).getBytes()
  }
}

object TestPostMockRoute extends Router {
  override def method: String = HttpMethod.POST.toString

  override def route: String = s"/mock/post"

  override def handleRequest(request: FullHttpRequest): Array[Byte] = {
    val resp = mutable.Map.empty[String, Any]
    val headers = request.headers().entries().asScala.foldLeft(Map.empty[String, String]){ (map, entry) =>
      map ++ Map(entry.getKey -> entry.getValue)
    }

    val content = request.content().toString(Charset.defaultCharset())
    val bodyMap = JsonUtils.parseFrom[Map[String, Any]](content)
    
    resp += ("requestHeaders" -> headers)
    resp += ("body" -> bodyMap)

    JsonUtils.toJson(resp).getBytes()
  }
}
