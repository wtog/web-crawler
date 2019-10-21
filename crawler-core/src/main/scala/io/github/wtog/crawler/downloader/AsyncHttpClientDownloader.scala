package io.github.wtog.crawler.downloader

import io.github.wtog.crawler.downloader.proxy.ProxyDTO
import io.github.wtog.crawler.exceptions.IllegalArgumentsException
import io.github.wtog.crawler.processor.{ Page, RequestSetting }
import io.netty.handler.codec.http.{ DefaultHttpHeaders, HttpHeaderNames }
import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient._
import org.asynchttpclient.proxy.ProxyServer

import scala.concurrent.{ Future, Promise, TimeoutException }

/**
  * @author : tong.wang
  * @since : 5/16/18 11:13 PM
  * @version : 1.0.0
  */
object AsyncHttpClientDownloader extends Downloader[AsyncHttpClient] {

  private[this] def buildRequest(driver: AsyncHttpClient, request: RequestSetting, proxyOpt: Option[ProxyDTO] = None): BoundRequestBuilder = {
    proxyOpt.foreach { proxy =>
      buildProxy(proxy)(p => new ProxyServer.Builder(p.host, p.port).build())
    }

    val builder = builderMethod(driver, request.url.get, request.method)

    val httpHeaders = new DefaultHttpHeaders
    request.headers.foreach { case (k, v) ⇒ httpHeaders.add(k, v) }
    httpHeaders.add(HttpHeaderNames.USER_AGENT, request.userAgent)
    httpHeaders.add(HttpHeaderNames.ACCEPT_CHARSET, request.charset)
    builder.setHeaders(httpHeaders)

  }

  def builderMethod(driver: AsyncHttpClient, url: String, method: String) =
    method.toUpperCase match {
      case "GET" ⇒
        driver.prepareGet(url)
      case "POST" ⇒
        driver.preparePost(url)
      case other ⇒
        logger.warn(s"unknown httpmethod ${other}")
        throw IllegalArgumentsException(other)
    }

  override def doDownload(request: RequestSetting): Future[Page] = {
    val response = executeRequest(request) { proxyOpt =>
      val promise = Promise[Response]

      val client = getOrCreateClient(request)
      buildRequest(client.driver, request, proxyOpt).execute(new AsyncCompletionHandler[Response]() {
        override def onCompleted(response: Response): Response = {
          promise.success(response)
          client.decrement()
          response
        }

        override def onThrowable(t: Throwable): Unit = {
          if (t.isInstanceOf[TimeoutException]) {
            logger.error("download error ", t)
          }
          client.decrement()
          promise.failure(t)
        }
      })

      promise.future
    }

    response.map { r =>
      pageResult(request, Some(r.getResponseBodyAsBytes), r.getStatusCode == 200, Some(s"return ${r.getStatusCode}"))
    }(io.github.wtog.crawler.actor.ExecutionContexts.downloadDispatcher)
  }

  def closeClient(): Unit = closeDownloaderClient { client =>
    client.close()
  }

  override protected def getOrCreateClient(requestSetting: RequestSetting) =
    getDownloaderClient(requestSetting.domain) {
      asyncHttpClient(
        new DefaultAsyncHttpClientConfig.Builder()
          .setRequestTimeout(requestSetting.timeOut.toMillis.toInt)
          .setConnectTimeout(requestSetting.timeOut.toMillis.toInt)
          .setFollowRedirect(true)
          .setConnectionPoolCleanerPeriod(5)
          .build()
      )
    }
}
