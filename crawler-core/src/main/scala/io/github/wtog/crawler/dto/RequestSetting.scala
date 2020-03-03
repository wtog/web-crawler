package io.github.wtog.crawler.dto

import java.nio.charset.Charset

import io.netty.handler.codec.http.HttpMethod

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

/**
  * @author : tong.wang
  * @since : 1/2/20 9:43 PM
  * @version : 1.0.0
  */
case class RequestSetting(
    domain: String = "",
    method: String = HttpMethod.GET.toString,
    url: Option[String] = None,
    userAgent: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.75 Safari/537.36",
    requestBody: Option[String] = None,
    headers: mutable.Map[String, String] = mutable.Map.empty[String, String],
    sleepTime: Duration = 1 seconds,
    cookies: Option[Map[String, String]] = None,
    charset: String = Charset.defaultCharset().name(),
    retryTime: Int = 0,
    timeOut: Duration = 3 seconds,
    useProxy: Boolean = false,
    xhrRequests: Set[String] = Set.empty[String]) {

  def withUrlAndMethod(url: String, method: String = HttpMethod.GET.toString): RequestSetting =
    this.copy(url = Some(url), method = method)

  def withUrl(url: String): RequestSetting = this.copy(url = Some(url))

  def withSleepTime(sleepTime: Duration): RequestSetting = this.copy(sleepTime = sleepTime)

  def withHeaders(extraHeaders: Map[String, String]): RequestSetting = {
    this.headers ++= extraHeaders.toSeq
    this
  }

  def addHeader(header: String, value: String): RequestSetting = {
    this.headers += (header -> value)
    this
  }

  def withMethodAndRequestBody(method: String, requestBody: Option[String]): RequestSetting =
    this.copy(method = method, requestBody = requestBody)

  def withRequestUri(requestUri: RequestUri): RequestSetting = {
    val basic = this.copy(
      url = Some(requestUri.url),
      method = requestUri.method,
      requestBody = requestUri.requestBody,
      xhrRequests = requestUri.xhrRequests
    )

    requestUri.headers.fold(basic) { extra ⇒
      basic.withHeaders(extra)
    }
  }

  def withXhrRequests(xhrRequest: String*): RequestSetting = {
    val requests = xhrRequest.foldLeft(this.xhrRequests) { (xhrRequests, xhrRequest) =>
      xhrRequests + xhrRequest
    }
    this.copy(xhrRequests = requests)
  }

  override def toString: String = {
    val fields = this.getClass.getDeclaredFields
      .map { field =>
        val value = field.get(this) match {
          case v: Option[Any] =>
            v.getOrElse("")
          case v =>
            v
        }

        (s"${field.getName}: $value", value)
      }
      .collect {
        case (v: String, t: String) if !t.isEmpty           => v
        case (v: String, t: Any) if !t.isInstanceOf[String] => v
      }

    s"${fields.mkString(", ")}"
  }
}

case class RequestUri(
                       url: String,
                       method: String = HttpMethod.GET.toString,
                       requestBody: Option[String] = None,
                       headers: Option[Map[String, String]] = None,
                       xhrRequests: Set[String] = Set.empty[String])
