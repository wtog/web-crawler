package io.github.wtog.crawler.pipeline

import java.util.concurrent.atomic.AtomicBoolean

import io.github.wtog.utils.logger.Logging

/**
  * @author : tong.wang
  * @since : 5/16/18 9:09 PM
  * @version : 1.0.0
  */
trait Pipeline extends Logging {

  private val inited = new AtomicBoolean(false)

  def open(): Unit = {
    if (!inited.getAndSet(true)) {
      init()
    }
  }

  protected def init(): Unit = ()

  def process[Result](pageResultItem: (String, Result)): Unit
}
