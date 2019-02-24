package io.github.wtog.downloader

import java.security.cert.X509Certificate

import io.github.wtog.downloader.proxy.ProxyDTO
import io.github.wtog.exceptions.NonNullArgumentsException
import io.github.wtog.processor.{ Page, RequestSetting }
import io.github.wtog.utils.UrlUtils
import javax.net.ssl.{ SSLContext, TrustManager, X509TrustManager }
import org.apache.http.auth.{ AuthState, UsernamePasswordCredentials }
import org.apache.http.client.config.{ CookieSpecs, RequestConfig }
import org.apache.http.client.methods._
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.config.{ RegistryBuilder, SocketConfig }
import org.apache.http.conn.socket.{ ConnectionSocketFactory, PlainConnectionSocketFactory }
import org.apache.http.conn.ssl.{ DefaultHostnameVerifier, SSLConnectionSocketFactory }
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client._
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.protocol.HttpContext
import org.apache.http.util.EntityUtils
import org.apache.http.{ HttpHost, HttpRequest, HttpRequestInterceptor, HttpResponse }
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.Future

/**
 * @author : tong.wang
 * @since : 5/19/18 11:03 PM
 * @version : 1.0.0
 */
object ApacheHttpClientDownloader extends Downloader {
  var clientsPool: Map[String, CloseableHttpClient] = Map()

  override def download(request: RequestSetting): Future[Page] = {
    import io.github.wtog.actor.ExecutionContexts.downloadDispatcher

    Future {
      def getResponse(p: Option[ProxyDTO]): CloseableHttpResponse = {
        val (httpUriRequest, httpClientContext) = HttpUriRequestConverter.convert(request, p)
        clientsDomain(request).execute(httpUriRequest, httpClientContext)
      }

      val httpResponse = getResponseWithProxyOrNot(request, getResponse)

      httpResponse.getStatusLine.getStatusCode match {
        case 200 ⇒
          val byteArray = EntityUtils.toByteArray(Option(httpResponse.getEntity).getOrElse(throw NonNullArgumentsException("apache downloader return empty content")))
          Page(requestSetting = request, bytes = Some(byteArray))
        case other ⇒
          logger.warn(s"failed download ${request.url.get} ${other}")
          Page(requestSetting = request, isDownloadSuccess = false)
      }
    }
  }

  def clientsDomain(requestSetting: RequestSetting): CloseableHttpClient = {
    val domain = requestSetting.domain

    clientsPool.getOrElse(domain, ApacheHttpClientGenerator.generateClient(requestSetting))
  }
}

object HttpUriRequestConverter {

  def convert(request: RequestSetting, proxy: Option[ProxyDTO]): (HttpUriRequest, HttpContext) = {
    val requestUrl = request.url.get

    val requestConfig = RequestConfig.custom
    val httpContext = new HttpClientContext
    proxy.foreach { p ⇒
      requestConfig.setProxy(new HttpHost(p.host, p.port))
      val authState = new AuthState
      authState.update(new BasicScheme(), new UsernamePasswordCredentials(p.username.getOrElse(""), p.password.getOrElse("")))
      httpContext.setAttribute(HttpClientContext.PROXY_AUTH_STATE, authState)
    }

    val requestBuilder = selectRequestMethod(request).setUri(UrlUtils.fixIllegalCharacterInUrl(requestUrl)).setHeader("Host", request.domain)
    requestBuilder.setConfig(
      requestConfig
        .setConnectionRequestTimeout(request.timeOut)
        .setSocketTimeout(request.timeOut)
        .setConnectTimeout(request.timeOut)
        .setCookieSpec(CookieSpecs.STANDARD)
        .build)

    request.headers.foreach { case (key, value) ⇒ requestBuilder.addHeader(key, value) }

    request.cookies foreach { cookie ⇒
      val cookieStore = new BasicCookieStore
      cookie.foreach {
        case (name, value) ⇒ {
          val cook = new BasicClientCookie(name, value)
          cook.setDomain(UrlUtils.removePort(requestUrl))
          cookieStore.addCookie(cook)
        }
      }
      httpContext.setCookieStore(cookieStore)
    }

    (requestBuilder.build, httpContext)
  }

  private def selectRequestMethod(request: RequestSetting): RequestBuilder = {
    request.method.toUpperCase match {
      case "POST"   ⇒ addFormParams(RequestBuilder.post, request)
      case "HEAD"   ⇒ RequestBuilder.head
      case "OPTION" ⇒ RequestBuilder.options
      case "PUT"    ⇒ addFormParams(RequestBuilder.put, request)
      case "TRACE"  ⇒ RequestBuilder.trace
      case _        ⇒ RequestBuilder.get()
    }
  }

  private def addFormParams(requestBuilder: RequestBuilder, request: RequestSetting) = {
    requestBuilder.setEntity(new StringEntity(request.requestBody.get))
    requestBuilder
  }

}

object ApacheHttpClientGenerator {
  val connectionManager: PoolingHttpClientConnectionManager = {
    def buildSSLConnectionSocketFactory: SSLConnectionSocketFactory = {
      def createIgnoreVerifySSL: SSLContext = {
        val trustManager = new X509TrustManager() {
          override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {}

          override def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {}

          override def getAcceptedIssuers: Array[X509Certificate] = null
        }

        val sc = SSLContext.getInstance("SSLv3")
        sc.init(null, Array[TrustManager](trustManager), null)
        sc
      }

      new SSLConnectionSocketFactory(createIgnoreVerifySSL, Array[String]("SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"), null, new DefaultHostnameVerifier); // 优先绕过安全证书
    }

    val reg = RegistryBuilder.create[ConnectionSocketFactory].register("http", PlainConnectionSocketFactory.INSTANCE).register("https", buildSSLConnectionSocketFactory).build
    val connectionManager = new PoolingHttpClientConnectionManager(reg)
    connectionManager.setDefaultMaxPerRoute(100)
    connectionManager
  }

  def generateClient(requestHeaders: RequestSetting): CloseableHttpClient = {
    val httpClientBuilder = HttpClients.custom

    httpClientBuilder.setConnectionManager(connectionManager)
    httpClientBuilder.setUserAgent(requestHeaders.userAgent)

    if (requestHeaders.useGzip) {
      httpClientBuilder.addInterceptorFirst(new HttpRequestInterceptor() {
        override def process(request: HttpRequest, context: HttpContext): Unit = {
          if (!request.containsHeader("Accept-Encoding")) request.addHeader("Accept-Encoding", "gzip")
        }
      })
    }

    //解决post/redirect/post 302跳转问题
    httpClientBuilder.setRedirectStrategy(CustomRedirectStrategy)

    val socketConfigBuilder = SocketConfig.custom
    socketConfigBuilder.setSoKeepAlive(true).setTcpNoDelay(true)
    socketConfigBuilder.setSoTimeout(requestHeaders.timeOut)
    val socketConfig = socketConfigBuilder.build
    httpClientBuilder.setDefaultSocketConfig(socketConfig)
    connectionManager.setDefaultSocketConfig(socketConfig)
    httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(requestHeaders.retryTimes, true))
    generateCookie(httpClientBuilder, requestHeaders)

    httpClientBuilder.build()
  }

  def generateCookie(httpClientBuilder: HttpClientBuilder, requestHeaders: RequestSetting) = {
    if (requestHeaders.disableCookieManagement) {
      httpClientBuilder.disableCookieManagement
    } else {
      val cookieStore = new BasicCookieStore
      requestHeaders.cookies foreach { cookies ⇒
        cookies.foreach(it ⇒ {
          val cookie = new BasicClientCookie(it._1, it._2)
          cookie.setDomain(requestHeaders.domain)
          cookieStore.addCookie(cookie)
        })
      }

      httpClientBuilder.setDefaultCookieStore(cookieStore)
    }
  }
}

object CustomRedirectStrategy extends LaxRedirectStrategy {
  private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def getRedirect(request: HttpRequest, response: HttpResponse, context: HttpContext): HttpUriRequest = {
    val uri = getLocationURI(request, response, context)
    val method = request.getRequestLine.getMethod
    if ("post".equalsIgnoreCase(method)) {
      try {
        val httpRequestWrapper = request.asInstanceOf[HttpRequestWrapper]
        httpRequestWrapper.setURI(uri)
        httpRequestWrapper.removeHeaders("Content-Length")
        return httpRequestWrapper
      } catch {
        case e: Exception ⇒
          logger.error("强转为HttpRequestWrapper出错", e)
      }
      new HttpPost(uri)
    } else new HttpGet(uri)
  }
}

