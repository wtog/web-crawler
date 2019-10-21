package io.github.wtog.crawler.pipeline

/**
  * @author : tong.wang
  * @since : 10/18/19 10:07 PM
  * @version : 1.0.0
  */
object ConsolePipeline extends Pipeline {
  def process[R](pageResultItem: (String, R)): Unit = {
    val (url, result) = pageResultItem
    logger.trace(s"crawl result: ${url} - ${result}")
  }
}
