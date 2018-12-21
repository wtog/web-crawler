package io.github.wtog.downloader

import io.github.wtog.downloader.proxy.ProxyDTO
import io.netty.handler.codec.http.DefaultHttpHeaders
import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient._
import io.github.wtog.exceptions.{ IllegalArgumentsException, NonNullArgumentsException }
import io.github.wtog.processor.{ Page, RequestHeaders }
import org.asynchttpclient.proxy.ProxyServer

import scala.concurrent.{ Future, Promise }

/**
 * @author : tong.wang
 * @since : 5/16/18 11:13 PM
 * @version : 1.0.0
 */
object AsyncHttpClientDownloader extends Downloader {

  var clientsPool: Map[String, AsyncHttpClient] = Map()

  def clientPrepare(requestBuilder: BoundRequestBuilder, requestHeaders: RequestHeaders): BoundRequestBuilder = {
    import io.netty.handler.codec.http.HttpHeaders._

    val httpHeaders = new DefaultHttpHeaders
    requestHeaders.headers.foreach { it ⇒ httpHeaders.add(it._1, it._2) }

    httpHeaders.add(Names.USER_AGENT, requestHeaders.userAgent)
    httpHeaders.add(Names.ACCEPT_CHARSET, requestHeaders.charset.get)
    requestBuilder.setHeaders(httpHeaders)
    requestBuilder
  }

  override def download(request: RequestHeaders): Future[Page] = {
    def getResponse(p: Option[ProxyDTO]): Future[Response] = {
      val requestGeneral = request.requestHeaderGeneral.getOrElse(throw NonNullArgumentsException("requestGeneral"))
      val domain = request.domain

      val requestBuilder: BoundRequestBuilder = requestGeneral.method.toUpperCase match {
        case "GET" ⇒
          downloadClients(domain).prepareGet(requestGeneral.url.get)
        case "POST" ⇒
          downloadClients(domain).preparePost(requestGeneral.url.get)
        case other ⇒
          logger.warn(s"unknown httpmethod ${other}")
          throw IllegalArgumentsException(other)
      }

      p foreach { proxy ⇒ requestBuilder.setProxyServer(new ProxyServer.Builder(proxy.host, proxy.port).build()) }

      val promise = Promise[Response]
      clientPrepare(requestBuilder, request)
        .execute(new AsyncCompletionHandler[Response]() {
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
        Page(requestGeneral = request.requestHeaderGeneral.get, bytes = Some(response.getResponseBodyAsBytes))
      case response ⇒
        logger.warn(s"failed download ${request.requestHeaderGeneral.get} ${response.getStatusCode}")
        Page(requestGeneral = request.requestHeaderGeneral.get, isDownloadSuccess = false)

    }

  }

  def downloadClients(domain: String): AsyncHttpClient = {
    if (!clientsPool.contains(domain)) {
      clientsPool += (domain -> asyncHttpClient())
    }
    clientsPool(domain)
  }
}
