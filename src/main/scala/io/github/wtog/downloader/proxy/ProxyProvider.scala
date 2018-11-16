package io.github.wtog.downloader.proxy

import java.net.{ HttpURLConnection, InetSocketAddress, URL }
import java.util.Objects
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicInteger }

import akka.actor.Cancellable
import io.github.wtog.actor.ActorManager
import io.github.wtog.downloader.proxy.ProxyProvider.checkUrl
import io.github.wtog.downloader.proxy.ProxyStatusEnums.ProxyStatusEnums
import io.github.wtog.downloader.proxy.crawler.{ A2UPageProcessor, Data5UPageProcessor }
import io.github.wtog.spider.{ Spider, SpiderPool }
import org.apache.http.client.methods.CloseableHttpResponse
import org.slf4j.{ Logger, LoggerFactory }

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author : tong.wang
 * @since : 5/20/18 11:08 AM
 * @version : 1.0.0
 */
object ProxyProvider {
  private lazy val logger: Logger = LoggerFactory.getLogger(ProxyProvider.getClass)

  private var crawlProxyCronJob: ListBuffer[Cancellable] = ListBuffer.empty[Cancellable]
  val checkUrl: URL = Try(new URL("http://www.baidu.com")).get
  val proxySpiderCrawling: AtomicBoolean = new AtomicBoolean(false)
  var proxyList: Set[ProxyDTO] = Set()

  val proxyCrawlerList = List(
    (Spider(name = "proxy-a2u", pageProcessor = A2UPageProcessor), 10 seconds),
    (Spider(name = "proxy-data5", pageProcessor = Data5UPageProcessor), 10 seconds))

  lazy val listProxyStatus = ActorManager.system.scheduler.schedule(5 seconds, 15 seconds)({
    proxyList --= proxyList.filter(p ⇒ p.status == ProxyStatusEnums.IDEL && p.usabilityCheck() < 0.5 && p.checkTimes.get() > 6)
    if (logger.isDebugEnabled()) {
      logger.debug(s"proxy sum: ${proxyList.size}, using: ${proxyList.count(p ⇒ p.status == ProxyStatusEnums.USING)}")
    }

    if (SpiderPool.fetchAllUsingProxySpiders().length == 0) {
      proxyCrawlerList.foreach { case (spider, _) ⇒ spider.stop() }
      crawlProxyCronJob.foreach(_.cancel())
    }
  })

  def startProxyCrawl() = {
    if (!proxySpiderCrawling.getAndSet(true)) {
      proxyCrawlerList.foreach {
        case (spider, scheduleTime) ⇒
          crawlProxyCronJob.append(ActorManager.system.scheduler.schedule(0 seconds, scheduleTime)(spider.start()))
      }
      listProxyStatus
    }
  }

  def getProxy: Option[ProxyDTO] = {
    if (proxyList.nonEmpty) {
      proxyList.filter(_.status == ProxyStatusEnums.IDEL).find(it ⇒ {
        val usability = it.usabilityCheck() > 0.5

        if (it.checkTimes.get() > 10 && !usability)
          proxyList -= it

        usability
      })
    } else {
      None
    }
  }

  def requestWithProxy[T <: CloseableHttpResponse](useProxy: Boolean, httpRequest: Option[ProxyDTO] ⇒ T): T = {
    if (useProxy) {
      getProxy match {
        case proxy @ Some(p) ⇒
          try {
            p.status = ProxyStatusEnums.USING
            val proxyRequest = httpRequest(proxy)
            p.status = ProxyStatusEnums.IDEL
            proxyRequest
          } catch {
            case NonFatal(e) ⇒
              logger.warn(s"failed to execute request using proxy: ${e.getLocalizedMessage}")
              Future {
                p.usabilityCheck()
              }
              httpRequest(None)
          }
        case None ⇒
          httpRequest(None)
      }
    } else {
      httpRequest(None)
    }
  }
}

final case class ProxyDTO(
    host:          String,
    port:          Int,
    username:      Option[String]   = None,
    password:      Option[String]   = None,
    var status:    ProxyStatusEnums = ProxyStatusEnums.IDEL,
    checkTimes:    AtomicInteger    = new AtomicInteger(0),
    var usability: Float            = 0F) {

  val successTimes: AtomicInteger = new AtomicInteger(0)

  def usabilityCheck(): Float = {
    Try {
      import java.net.Proxy
      val proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port))
      val connection = checkUrl.openConnection(proxy).asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(2000)
      connection.setReadTimeout(2000)
      connection.setDoOutput(true)

      connection.getResponseCode match {
        case 200 ⇒
          successTimes.incrementAndGet() / checkTimes.get()
        case _ ⇒
          successTimes.get() / checkTimes.incrementAndGet()
      }
    }.recover {
      case NonFatal(_) ⇒
        successTimes.get() / checkTimes.incrementAndGet()
    }.get
  }

  override def hashCode(): Int = Objects.hash(this.host.asInstanceOf[Object], this.port.asInstanceOf[Object])

  override def equals(obj: scala.Any): Boolean = (obj) match {
    case t: ProxyDTO ⇒
      t.host == this.host && t.port == this.port
    case _ ⇒ false
  }

  override def toString: String = s"${host}:${port}"
}

object ProxyStatusEnums extends Enumeration {
  type ProxyStatusEnums = Value
  val USING = Value("using")
  val IDEL = Value("idel")
}
