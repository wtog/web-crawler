package io.github.wtog.spider

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

import io.github.wtog.actor.ActorManager
import io.github.wtog.downloader.proxy.ProxyProvider
import io.github.wtog.schedule.ScheduleJobs

/**
 * @author : tong.wang
 * @since : 9/15/18 10:51 AM
 * @version : 1.0.0
 */
object SpiderPool {
  val spiders = new ConcurrentHashMap[String, Spider]()

  val spiderScheduleTaskRunning = new AtomicBoolean(false)

  def addSpider(spider: Spider) = {
    if (spiders.contains(spider.name)) throw new IllegalArgumentException(s"duplicate spider name ${spider.name}")

    spiders.put(spider.name, spider)
    if (!spiderScheduleTaskRunning.getAndSet(true)) {
      runScheduleTask()
    }

    if (spider.pageProcessor.requestHeaders.useProxy) {
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
    fetchAllSpiders().filter(_.pageProcessor.requestHeaders.useProxy)
  }

  def runScheduleTask(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    ActorManager.system.scheduler.schedule(1 minutes, 1 minutes)({
      fetchAllSpiders().foreach(ScheduleJobs.addSpiderScheduleJob)
    })
  }
}
