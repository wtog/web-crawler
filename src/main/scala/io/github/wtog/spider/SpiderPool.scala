package io.github.wtog.spider

import java.util.concurrent.ConcurrentHashMap

import io.github.wtog.downloader.proxy.ProxyProvider

/**
 * @author : tong.wang
 * @since : 9/15/18 10:51 AM
 * @version : 1.0.0
 */
object SpiderPool {
  val spiders = new ConcurrentHashMap[String, Spider]()

  def addSpider(spider: Spider) = {
    if (spiders.contains(spider.name)) throw new IllegalArgumentException(s"duplicate spider name ${spider.name}")

    spiders.put(spider.name, spider)
    if (spider.pageProcessor.requestHeaders.useProxy) ProxyProvider.startProxyCrawl()
  }

  def removeSpider(spider: Spider) = {
    spiders.remove(spider.name)
  }

  def fetchAllSpiders() = {
    spiders.values().toArray()
  }

  def fetchAllUsingProxySpiders() = {
    fetchAllSpiders().filter(_.asInstanceOf[Spider].pageProcessor.requestHeaders.useProxy)
  }
}
