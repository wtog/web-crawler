package io.github.wtog.downloader

import io.netty.handler.codec.http.DefaultHttpHeaders
import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient._
import io.github.wtog.exceptions.{ IllegalArgumentsException, NonNullArgumentsException }
import io.github.wtog.processor.{ Page, RequestHeaders }

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
    requestHeaders.headers match {
      case Some(headers) ⇒
        headers.foreach { it ⇒ httpHeaders.add(it._1, it._2) }
      case None ⇒ logger.debug("no extra headers")
    }

    httpHeaders.add(Names.USER_AGENT, requestHeaders.userAgent)
    httpHeaders.add(Names.ACCEPT_CHARSET, requestHeaders.charset.get)
    requestBuilder.setHeaders(httpHeaders)
    requestBuilder
  }

  override def download(request: RequestHeaders): Future[Page] = {
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

    val promise = Promise[Page]
    clientPrepare(requestBuilder, request).execute(new AsyncCompletionHandler[Response]() {
      override def onCompleted(response: Response): Response = {
        if (response.getStatusCode == 200) {
          promise.success(Page(requestGeneral = requestGeneral, isDownloadSuccess = true, bytes = Some(response.getResponseBodyAsBytes)))
        } else {
          promise.failure(new IllegalStateException(s"http download failed ${response.getStatusCode}"))
        }
        response
      }

      override def onThrowable(t: Throwable): Unit = {
        logger.error("http download failed ", t.getMessage)
      }
    })

    promise.future
  }

  def downloadClients(domain: String): AsyncHttpClient = {
    if (!clientsPool.contains(domain)) {
      clientsPool += (domain -> asyncHttpClient())
    }
    clientsPool(domain)
  }
}
