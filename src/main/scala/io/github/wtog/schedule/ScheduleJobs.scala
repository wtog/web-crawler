package io.github.wtog.schedule

import io.github.wtog.spider.{ Spider, SpiderPool }
import org.quartz._
import org.quartz.impl.StdSchedulerFactory
import org.quartz.JobBuilder
import org.quartz.impl.matchers.GroupMatcher

/**
 * @author : tong.wang
 * @since : 2018-12-08 23:48
 * @version : 1.0.0
 */
object ScheduleJobs {
  private val scheduler = new StdSchedulerFactory().getScheduler()

  def getScheduledJobs = {
    import collection.JavaConverters._

    val groupNames = scheduler.getJobGroupNames.asScala

    groupNames.flatMap { gn ⇒
      val jobKey = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(gn)).asScala

      jobKey.map(_.getName)
    }
  }

  def addSpiderScheduleJob(spider: Spider) = {
    if (!getScheduledJobs.contains(spider.name)) {
      spider.pageProcessor.cronExpression.foreach { cronExpression ⇒
        val job = JobBuilder.newJob(classOf[SpiderJob]).withIdentity(spider.name).build
        val trigger = TriggerBuilder.newTrigger()
          .withIdentity(spider.pageProcessor.requestHeaders.domain)
          .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
          .build()

        scheduler.scheduleJob(job, trigger)
        scheduler.start()
      }
    }
  }

}

class SpiderJob() extends Job {
  override def execute(jobExecutionContext: JobExecutionContext): Unit = {
    SpiderPool.getSpiderByName(jobExecutionContext.getJobDetail.getKey.getName).foreach(_.restart())
  }
}
