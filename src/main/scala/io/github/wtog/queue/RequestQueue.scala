package io.github.wtog.queue

import io.github.wtog.processor.RequestSetting
import org.slf4j.{ Logger, LoggerFactory }

/**
  * @author : tong.wang
  * @since : 5/16/18 10:03 PM
  * @version : 1.0.0
  */
trait RequestQueue {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def push(request: RequestSetting)

  def poll(): Option[RequestSetting]

  def take(): Option[RequestSetting]

  def isEmpty: Boolean

  def nonEmpty: Boolean = !isEmpty
}
