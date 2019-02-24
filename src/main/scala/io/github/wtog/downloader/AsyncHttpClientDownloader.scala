package io.github.wtog.downloader

import java.util.concurrent.ConcurrentHashMap

import io.github.wtog.downloader.proxy.ProxyDTO
import io.github.wtog.exceptions.IllegalArgumentsException
import io.github.wtog.processor.{ Page, RequestSetting }
import io.netty.handler.codec.http.{ DefaultHttpHeaders, HttpHeaderNames }
import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient._
import org.asynchttpclient.proxy.ProxyServer

import scala.concurrent.{ Future, Promise }

/**
 * @author : tong.wang
 * @since : 5/16/18 11:13 PM
 * @version : 1.0.0
 */
object AsyncHttpClientDownloader extends Downloader {

  val clientsPool = new ConcurrentHashMap[String, AsyncHttpClient]

  private[this] def clientPrepare(requestBuilder: BoundRequestBuilder, requestHeaders: RequestSetting): BoundRequestBuilder = {

    val httpHeaders = new DefaultHttpHeaders
    requestHeaders.headers.foreach { case (k, v) ⇒ httpHeaders.add(k, v) }

    httpHeaders.add(HttpHeaderNames.USER_AGENT, requestHeaders.userAgent)
    httpHeaders.add(HttpHeaderNames.ACCEPT_CHARSET, requestHeaders.charset.get)
    requestBuilder.setHeaders(httpHeaders)
    requestBuilder
  }

  override def download(request: RequestSetting): Future[Page] = {
    def getResponse(p: Option[ProxyDTO]): Future[Response] = {
      val domain = request.domain

      val downclients = downloadClients(domain, Some(request))
      val requestBuilder: BoundRequestBuilder = request.method.toUpperCase match {
        case "GET" ⇒
          downclients.prepareGet(request.url.get)
        case "POST" ⇒
          downclients.preparePost(request.url.get)
        case other ⇒
          logger.warn(s"unknown httpmethod ${other}")
          throw IllegalArgumentsException(other)
      }

      p foreach { proxy ⇒ requestBuilder.setProxyServer(new ProxyServer.Builder(proxy.host, proxy.port).build()) }

      val promise = Promise[Response]

      clientPrepare(requestBuilder, request).execute(new AsyncCompletionHandler[Response]() {
        override def onCompleted(response: Response): Response = {
          promise.success(response)
          response
        }

        override def onThrowable(t: Throwable): Unit = {
          logger.error("http download failed ", t.getMessage)
        }
      })

      promise.future
    }

    import io.github.wtog.actor.ExecutionContexts.downloadDispatcher
    getResponseWithProxyOrNot[Future[Response]](request, getResponse).map {
      case response if response.getStatusCode == 200 ⇒
        Page(requestSetting = request, bytes = Some(response.getResponseBodyAsBytes))
      case response ⇒
        logger.warn(s"failed download ${request.url.get} ${response.getStatusCode}")
        Page(requestSetting = request, isDownloadSuccess = false)
    }

  }

  def downloadClients(domain: String, requestConfig: Option[RequestSetting] = None): AsyncHttpClient = {
    val clientCache = Option(clientsPool.get(domain))

    clientCache getOrElse {
      val client = requestConfig.fold(asyncHttpClient()) { config ⇒
        val builder = new DefaultAsyncHttpClientConfig.Builder()
        builder.setRequestTimeout(config.timeOut)
          .setConnectTimeout(config.timeOut)
          .setFollowRedirect(true)
          .setKeepAlive(true)
          .setConnectionPoolCleanerPeriod(5)
          .build()

        asyncHttpClient(builder)
      }

      clientsPool.put(domain, client)
      client
    }
  }
}
