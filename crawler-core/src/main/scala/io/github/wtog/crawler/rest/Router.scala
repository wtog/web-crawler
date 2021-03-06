package io.github.wtog.crawler.rest

import io.github.wtog.crawler.spider.SpiderPool
import io.github.wtog.utils.JsonUtils
import io.netty.handler.codec.http.FullHttpRequest

/**
  * @author : tong.wang
  * @since : 2019-08-28 10:38
  * @version : 1.0.0
  */
trait Router {

  def method: String

  def route: String

  def handleRequest(request: FullHttpRequest): Array[Byte]

  implicit def toBytes(content: String): Array[Byte] = content.getBytes()
}

object SpiderStatusRoute extends Router {
  override def method: String = "GET"

  override def route: String = "/spiders"

  override def handleRequest(request: FullHttpRequest): Array[Byte] = {
    val results = SpiderPool.fetchAllSpiders().foldLeft(List.empty[Map[String, Any]]) { (list, entry) =>
      entry.CrawlMetric.metricInfo() +: list
    }
    JsonUtils.toJson(results)
  }
}
