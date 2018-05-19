package wt.downloader

import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient.{AsyncCompletionHandler, AsyncHttpClient, Response}
import wt.exceptions.NonNullArgumentsException
import wt.processor.Page

/**
  * @author : tong.wang
  * @since : 5/16/18 11:13 PM
  * @version : 1.0.0
  */
object AsyncHttpClientDownloader extends Downloader {

  var clientsPool: Map[String, AsyncHttpClient] = Map()

  override def download(request: RequestHeaders): Page = {
    val requestGeneral = request.requestHeaderGeneral.getOrElse(throw NonNullArgumentsException("requestGeneral"))
    val domain = request.domain

    downloadClients(domain).prepare(requestGeneral.method, requestGeneral.url.getOrElse(throw NonNullArgumentsException("url")))
      .execute(new AsyncCompletionHandler[Page]() {
        override def onCompleted(response: Response): Page = {
          if (response.getStatusCode == 200) {
            Page(requestGeneral = requestGeneral, isDownloadSuccess = true, pageSource = Some(response.getResponseBody))
          } else {
            logger.warn(s"http download failed ${response.getStatusCode}")
            Page(requestGeneral = requestGeneral)
          }
        }

        override def onThrowable(t: Throwable): Unit = {
          logger.error("http download failed ", t.getMessage)
        }
    })
  }.get()

  def downloadClients(domain: String): AsyncHttpClient = {
    if (!clientsPool.contains(domain)){
      clientsPool += (domain -> asyncHttpClient())
    }
    clientsPool(domain)
  }
}
