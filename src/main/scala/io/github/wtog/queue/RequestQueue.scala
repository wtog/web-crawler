package io.github.wtog.queue

import org.slf4j.{Logger, LoggerFactory}
import io.github.wtog.processor.RequestHeaderGeneral

/**
  * @author : tong.wang
  * @since : 5/16/18 10:03 PM
  * @version : 1.0.0
  */
trait RequestQueue {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def push(request: RequestHeaderGeneral)

  def poll(): Option[RequestHeaderGeneral]

  def isEmpty: Boolean
}
