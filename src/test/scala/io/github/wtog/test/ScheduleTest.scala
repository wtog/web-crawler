package io.github.wtog.test

import java.util.concurrent.TimeUnit

import io.github.wtog.schedule.{ScheduleJob, ScheduleJobs}
import org.quartz.{Job, JobExecutionContext}

/**
  * @author : tong.wang
  * @since : 2019-05-12 23:11
  * @version : 1.0.0
  */
class ScheduleTest extends BaseTest with Job {

  val intervalPrintJob = ScheduleJob(jobName = "intervalPrintJob", cronExpression = "*/1 * * ? * *", classOf[ScheduleTest], groupName = Some("test"))

  override def execute(context: JobExecutionContext): Unit = {
    assert(context.getJobDetail.getKey == intervalPrintJob.jobKey)
  }

  test("addJob") {
    ScheduleJobs.addJob(intervalPrintJob)
    TimeUnit.SECONDS.sleep(2)
    ScheduleJobs.shutdown()
  }

}
