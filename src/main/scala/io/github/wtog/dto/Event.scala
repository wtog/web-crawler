package io.github.wtog.dto

import io.github.wtog.pipeline.Pipeline
import io.github.wtog.processor.{ Page, RequestSetting }
import io.github.wtog.spider.Spider

/**
  * @author : tong.wang
  * @since : 2019-05-01 22:56
  * @version : 1.0.0
  */
sealed trait Event

case class DownloadEvent(spider: Spider, request: RequestSetting) extends Event

case class ProcessorEvent(spider: Spider, page: Page) extends Event

case class PipelineEvent[R](pipelineList: Set[Pipeline], pageResultItems: (String, R)) extends Event
