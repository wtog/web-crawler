package io.github.wtog.crawler.dto

import io.github.wtog.crawler.pipeline.Pipeline
import io.github.wtog.crawler.spider.Spider
import org.apache.logging.log4j.scala.Logging

import scala.util.{ Failure, Success, Try }

/**
  * @author : tong.wang
  * @since : 2019-05-01 22:56
  * @version : 1.0.0
  */
sealed trait Event

case class DownloadEvent(spider: Spider, request: RequestSetting) extends Event

case class ProcessorEvent(spider: Spider, page: Page) extends Event

case class PipelineEvent[R](pipelineList: Set[Pipeline], pageResultItems: (String, R)) extends Event with Logging {
  def initPipelines(): Option[PipelineEvent[R]] = {
    val allInited = pipelineList
      .map { p =>
        Try(p.init()) match {
          case Success(_) => true
          case Failure(exception) =>
            logger.error(s"failed to init pipeline ${exception.getLocalizedMessage}")
            false
        }
      }
      .forall(_ == true)

    if (allInited) Some(this) else None
  }
}
