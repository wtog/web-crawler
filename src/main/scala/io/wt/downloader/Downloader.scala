package io.wt.downloader

import org.slf4j.{Logger, LoggerFactory}
import io.wt.processor.{Page, RequestHeaders}

import scala.concurrent.Future

/**
  * @author : tong.wang
  * @since : 5/16/18 9:56 PM
  * @version : 1.0.0
  */
trait Downloader {
  protected lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def download(request: RequestHeaders): Future[Page]
}


