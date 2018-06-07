package wt.pipeline

import java.util.concurrent.atomic.AtomicInteger

import org.slf4j.{Logger, LoggerFactory}


/**
  * @author : tong.wang
  * @since : 5/16/18 9:09 PM
  * @version : 1.0.0
  */
trait Pipeline {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def process(pageResultItem: (String, Map[String, Any]))
}


object ConsolePipeline extends Pipeline {
  override def process(pageResultItem: (String, Map[String, Any])): Unit = {
    val (url, result) = pageResultItem
    println(s"crawl result: ${url} - ${result}")
  }
}
