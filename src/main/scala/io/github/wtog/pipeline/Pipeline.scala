package io.github.wtog.pipeline

import org.slf4j.{ Logger, LoggerFactory }

/**
  * @author : tong.wang
  * @since : 5/16/18 9:09 PM
  * @version : 1.0.0
  */
trait Pipeline {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def process[Result](pageResultItem: (String, Result))
}

object ConsolePipeline extends Pipeline {

  def process[R](pageResultItem: (String, R)): Unit = {
    val (url, result) = pageResultItem
    logger.trace(s"crawl result: ${url} - ${result}")
  }
}
