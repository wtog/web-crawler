package io.github.wtog.downloader

import io.github.wtog.downloader.proxy.{ ProxyDTO, ProxyProvider }
import io.github.wtog.processor.{ Page, RequestSetting }
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.Future

/**
 * @author : tong.wang
 * @since : 5/16/18 9:56 PM
 * @version : 1.0.0
 */
trait Downloader {
  protected lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def download(request: RequestSetting): Future[Page]

  protected def getResponseWithProxyOrNot[HttpResponse](requestHeaders: RequestSetting, httpRequestWithProxy: (Option[ProxyDTO]) ⇒ HttpResponse): HttpResponse = {
    ProxyProvider.requestWithProxy[HttpResponse](requestHeaders.useProxy, httpRequestWithProxy)
  }

}

object Downloader {
  object ContentType {
    lazy val FORM_URLENCODED = Map("Content-Type" -> "application/x-www-form-urlencoded")
    lazy val TEXT_PLAIN = Map("Content-Type" -> "text/plain")
    lazy val TEXT_JSON = Map("Content-Type" -> "application/json")
  }
}

