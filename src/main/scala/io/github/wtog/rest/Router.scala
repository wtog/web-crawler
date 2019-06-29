package io.github.wtog.rest

import io.github.wtog.selector.HtmlParser
import io.github.wtog.spider.SpiderPool
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

  implicit def toBytes(content: String) = content.getBytes()
}

object SpiderStatusRoute extends Router {
  override def method: String = "GET"

  override def route: String = "/spiders"

  override def handleRequest(request: FullHttpRequest): Array[Byte] = {
    val results = SpiderPool.fetchAllSpiders().foldLeft(List.empty[Map[String, Any]]){ (list, entry) =>
      entry.CrawlMetric.metricInfo() +: list
    }
    HtmlParser.toJson(results)
  }
}
