package io.github.wtog.downloader

import io.github.wtog.downloader.proxy.{ ProxyDTO, ProxyProvider }
import io.github.wtog.processor.{ Page, RequestSetting }
import io.github.wtog.spider.Spider
import io.github.wtog.utils.{ ConfigUtils, RetryInfo }
import io.github.wtog.utils.RetryUtils._
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.Future
import scala.util.control.NonFatal

/**
  * @author : tong.wang
  * @since : 5/16/18 9:56 PM
  * @version : 1.0.0
  */
trait Downloader {
  protected lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val downloadRetryException: Seq[String] = ConfigUtils.getSeq[String]("download.retry.exception")

  def download(spider: Spider, request: RequestSetting): Future[Page] = {
    import io.github.wtog.actor.ExecutionContexts.downloadDispatcher
    futureRetryWhen(doDownload(requestSetting = request), retryTime = request.retryTime, RetryInfo(duration = request.sleepTime, downloadRetryException))
      .map { page =>
        spider.CrawlMetric.record(page.isDownloadSuccess, page.url)
        page
      }
      .recover {
        case NonFatal(e) =>
          spider.CrawlMetric.record(success = false, request.url.get)
          throw e
      }
  }
  protected def doDownload(requestSetting: RequestSetting): Future[Page]

  protected def buildProxy[P](proxyDto: ProxyDTO)(buildProxy: ProxyDTO => P): P = buildProxy(proxyDto)

  protected def executeRequest[HttpResponse](requestSetting: RequestSetting)(execute: Option[ProxyDTO] => Future[HttpResponse]): Future[HttpResponse] =
    if (requestSetting.useProxy) {
      execute(ProxyProvider.getProxy)
    } else {
      execute(None)
    }

}

object Downloader {
  object ContentType {
    lazy val FORM_URLENCODED = Map("Content-Type" -> "application/x-www-form-urlencoded")
    lazy val TEXT_PLAIN      = Map("Content-Type" -> "text/plain")
    lazy val TEXT_JSON       = Map("Content-Type" -> "application/json")
  }
}
