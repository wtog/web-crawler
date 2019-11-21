package io.github.wtog.crawler.spider

import java.util.concurrent.ConcurrentHashMap

import io.github.wtog.crawler.downloader.proxy.ProxyProvider
import io.github.wtog.crawler.schedule.{ ScheduleJob, ScheduleJobs }
import org.quartz.{ Job, JobExecutionContext }

/**
  * @author : tong.wang
  * @since : 9/15/18 10:51 AM
  * @version : 1.0.0
  */
object SpiderPool {
  val spiders = new ConcurrentHashMap[String, Spider]()

  def addSpider(spider: Spider): Unit = {
    spiders.putIfAbsent(spider.name, spider)

    spider.pageProcessor.cronExpression.foreach { cron ⇒
      ScheduleJobs.addJob(ScheduleJob(jobName = spider.name, cronExpression = cron, task = classOf[SpiderScheduleJob]))
    }

    if (spider.pageProcessor.requestSetting.useProxy) {
      ProxyProvider.startProxyCrawl()
    }
  }

  def removeSpider(spider: Spider): Spider = spiders.remove(spider.name)

  def getSpiderByName(name: String): Option[Spider] = Option(spiders.get(name))

  def fetchAllSpiders(): Array[Spider] = spiders.values().toArray().map(_.asInstanceOf[Spider])

  def fetchAllUsingProxySpiders(): Array[Spider] = fetchAllSpiders().filter(_.pageProcessor.requestSetting.useProxy)

}

class SpiderScheduleJob() extends Job {
  override def execute(jobExecutionContext: JobExecutionContext): Unit =
    SpiderPool
      .getSpiderByName(jobExecutionContext.getJobDetail.getKey.getName)
      .foreach(_.restart())
}
