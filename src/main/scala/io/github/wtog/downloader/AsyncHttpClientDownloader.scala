package io.github.wtog.downloader

import java.util.concurrent.ConcurrentHashMap

import io.github.wtog.downloader.proxy.ProxyDTO
import io.github.wtog.exceptions.IllegalArgumentsException
import io.github.wtog.processor.{ Page, RequestSetting }
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
object AsyncHttpClientDownloader extends Downloader {

  private[this] val clientsPool = new ConcurrentHashMap[String, AsyncHttpClient]

  private[this] def buildRequest(request: RequestSetting, proxyOpt: Option[ProxyDTO] = None): BoundRequestBuilder = {
    val downclients = downloadClients(request)

    val builder = request.method.toUpperCase match {
      case "GET" ⇒
        downclients.prepareGet(request.url.get)
      case "POST" ⇒
        downclients.preparePost(request.url.get)
      case other ⇒
        logger.warn(s"unknown httpmethod ${other}")
        throw IllegalArgumentsException(other)
    }

    proxyOpt.foreach { proxy =>
      buildProxy(proxy) { _ =>
        new ProxyServer.Builder(proxy.host, proxy.port).build()
      }
    }

    val httpHeaders = new DefaultHttpHeaders
    request.headers.foreach { case (k, v) ⇒ httpHeaders.add(k, v) }
    httpHeaders.add(HttpHeaderNames.USER_AGENT, request.userAgent)
    httpHeaders.add(HttpHeaderNames.ACCEPT_CHARSET, request.charset)
    builder.setHeaders(httpHeaders)
  }

  private[this] def downloadClients(requestConfig: RequestSetting): AsyncHttpClient = {
    val clientCache = Option(clientsPool.get(requestConfig.domain))

    val domain = requestConfig.domain

    clientCache.getOrElse {
      val client = asyncHttpClient(
        new DefaultAsyncHttpClientConfig.Builder()
          .setRequestTimeout(requestConfig.timeOut)
          .setConnectTimeout(requestConfig.timeOut)
          .setFollowRedirect(true)
          .setConnectionPoolCleanerPeriod(5)
          .build()
      )

      clientsPool.put(domain, client)
      client
    }
  }

  override def doDownload(request: RequestSetting): Future[Page] = {
    val response = executeRequest(request) { proxyOpt =>
      val promise = Promise[Response]

      buildRequest(request, proxyOpt).execute(
        new AsyncCompletionHandler[Response]() {
          override def onCompleted(response: Response): Response = {
            promise.success(response)
            response
          }

          override def onThrowable(t: Throwable): Unit = {
            if (t.isInstanceOf[TimeoutException]) {
              logger.error("download error ", t)
            }
            promise.failure(t)
          }
        }
      )

      promise.future
    }

    response.map {
      case response if response.getStatusCode == 200 ⇒
        Page(
          requestSetting = request,
          bytes = Some(response.getResponseBodyAsBytes)
        )
      case response ⇒
        if (logger.isDebugEnabled()) {
          logger.debug(s"download return ${response.getStatusCode} with ${request}")
        } else {
          logger.warn(s"failed download ${request.url.get} ${response.getStatusCode}")
        }
        Page(requestSetting = request, isDownloadSuccess = false)
    }(io.github.wtog.actor.ExecutionContexts.downloadDispatcher)

  }
}
