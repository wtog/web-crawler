package io.github.wtog.crawler.schedule

import org.quartz.impl.StdSchedulerFactory
import org.quartz.{ JobBuilder, _ }

/**
  * @author : tong.wang
  * @since : 2018-12-08 23:48
  * @version : 1.0.0
  */
object ScheduleJobs {
  private lazy val scheduler = new StdSchedulerFactory().getScheduler()

  def addJob[C <: Job](scheduleJob: ScheduleJob[C]): Unit =
    if (!scheduler.checkExists(scheduleJob.jobKey)) {
      val trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(scheduleJob.cronExpression)).build()
      val job     = JobBuilder.newJob(scheduleJob.task).withIdentity(scheduleJob.jobKey).build

      scheduler.scheduleJob(job, trigger)
      scheduler.startDelayed(1)
    }

  def shutdown() = scheduler.shutdown(true)
}

case class ScheduleJob[C <: Job](jobName: String, cronExpression: String, task: Class[C], groupName: Option[String] = None) {
  val group  = groupName.getOrElse(jobName)
  val jobKey = new JobKey(jobName, group)
}
