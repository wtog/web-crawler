package io.github.wtog.crawler.pipeline

import io.github.wtog.utils.logger.Logging

/**
  * @author : tong.wang
  * @since : 5/16/18 9:09 PM
  * @version : 1.0.0
  */
trait Pipeline extends Logging {

  def init(): Unit = Unit

  def process[Result](pageResultItem: (String, Result)): Unit
}
