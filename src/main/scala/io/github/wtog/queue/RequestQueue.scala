package io.github.wtog.queue

import java.util.concurrent.TimeUnit

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

  def poll(): Option[RequestSetting] =
    doPoll().map { r =>
      TimeUnit.MILLISECONDS.sleep(r.sleepTime.toMillis)
      r
    }

  protected def doPoll(): Option[RequestSetting]

  def isEmpty: Boolean

  def nonEmpty: Boolean = !isEmpty
}
