package io.github.wtog.spider

import java.util.concurrent.ConcurrentHashMap

import io.github.wtog.downloader.proxy.ProxyProvider
import io.github.wtog.schedule.ScheduleJobs

/**
 * @author : tong.wang
 * @since : 9/15/18 10:51 AM
 * @version : 1.0.0
 */
object SpiderPool {
  val spiders = new ConcurrentHashMap[String, Spider]()

  def addSpider(spider: Spider) = {
    spiders.putIfAbsent(spider.name, spider)

    spider.pageProcessor.cronExpression.foreach { _ ⇒
      ScheduleJobs.addSpiderScheduleJob(spider)
    }

    if (spider.pageProcessor.requestSetting.useProxy) {
      ProxyProvider.startProxyCrawl()
      ProxyProvider.listProxyStatus()
    }
  }

  def removeSpider(spider: Spider) = {
    spiders.remove(spider.name)
  }

  def getSpiderByName(name: String): Option[Spider] = {
    Option(spiders.get(name))
  }

  def fetchAllSpiders(): Array[Spider] = {
    spiders.values().toArray().map(_.asInstanceOf[Spider])
  }

  def fetchAllUsingProxySpiders() = {
    fetchAllSpiders().filter(_.pageProcessor.requestSetting.useProxy)
  }

}
