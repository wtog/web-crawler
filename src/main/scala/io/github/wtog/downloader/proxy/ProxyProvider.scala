package io.github.wtog.downloader.proxy

import java.net.{HttpURLConnection, InetSocketAddress, URL}
import java.util.Objects
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import io.github.wtog.Spider
import org.slf4j.{Logger, LoggerFactory}
import io.github.wtog.actor.ActorManager
import io.github.wtog.downloader.proxy.ProxyProvider.checkUrl
import io.github.wtog.downloader.proxy.ProxyStatusEnums.ProxyStatusEnums
import io.github.wtog.downloader.proxy.crawler.{A2UPageProcessor, Data5UPageProcessor}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal

/**
  * @author : tong.wang
  * @since : 5/20/18 11:08 AM
  * @version : 1.0.0
  */
object ProxyProvider {
  private lazy val logger: Logger = LoggerFactory.getLogger(ProxyProvider.getClass)

  val checkUrl: URL = Try(new URL("http://www.baidu.com")).get
  val proxySpiderCrawling: AtomicBoolean = new AtomicBoolean(false)

  var proxyList: Set[ProxyDTO] = Set()

  val proxyListStatus = {
    import scala.concurrent.ExecutionContext.Implicits.global
    ActorManager.system.scheduler.schedule(15 seconds, 15 seconds, new Runnable {
      override def run(): Unit = {
        proxyList = (proxyList -- proxyList.filter(p => p.status == ProxyStatusEnums.IDEL && p.usabilityCheck() < 0.5 && p.checkTimes.get() > 6))
        logger.info(s"proxy sum: ${proxyList.size}, using: ${proxyList.count(p => p.status == ProxyStatusEnums.USING)}")
      }
    })
  }

  val proxyCrawlerList = {
    List((Spider(pageProcessor = A2UPageProcessor), 30 seconds),
         (Spider(pageProcessor = Data5UPageProcessor), 30 seconds))
  }

  def startProxyCrawl() = {
    if (!proxySpiderCrawling.getAndSet(true)) {
      import scala.concurrent.ExecutionContext.Implicits.global

      proxyCrawlerList.foreach {
        case (spider, scheduleTime) => {
          ActorManager.system.scheduler.schedule(0 seconds, scheduleTime, new Runnable {
            override def run(): Unit = spider.start()
          })
        }
      }
    }
  }

  def getProxy: Option[ProxyDTO] = {
    startProxyCrawl()

    if (proxyList.nonEmpty) {
      proxyList.filter(_.status == ProxyStatusEnums.IDEL).find(it => {
        val usability = it.usabilityCheck() > 0.5

        if (it.checkTimes.get() > 10 && !usability) proxyList -= it

        usability
      })
    } else {
      None
    }
  }

  def requestWithProxy[T <: Any](useProxy: Boolean, httpRequest: Option[ProxyDTO] => T): T = {
    import io.github.wtog.actor.ExecutionContexts.downloadDispatcher
    if (useProxy) {
      getProxy match {
        case proxy @ Some(p) =>
          try {
            p.status = ProxyStatusEnums.USING
            val proxyRequest = httpRequest(proxy)
            p.status = ProxyStatusEnums.IDEL
            proxyRequest
          } catch {
            case NonFatal(e) =>
              logger.warn(s"failed to execute request using proxy: ${e.getLocalizedMessage}")
              Future { p.usabilityCheck() }
              httpRequest(None)
          }
        case None =>
          httpRequest(None)
      }
    } else {
      httpRequest(None)
    }
  }
}

case class ProxyDTO(host: String,
                    port: Int,
                    username: Option[String] = None,
                    password: Option[String] = None,
                    var status: ProxyStatusEnums = ProxyStatusEnums.IDEL,
                    checkTimes: AtomicInteger = new AtomicInteger(0),
                    var usability: Float = 0F) {

  val successTimes: AtomicInteger = new AtomicInteger(0)

  def usabilityCheck(): Float = {
    usability = Try {
      import java.net.Proxy
      val proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port))
      val connection = checkUrl.openConnection(proxy).asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(2000)
      connection.setReadTimeout(2000)
      connection.setDoOutput(true)

      connection.getResponseCode match {
        case 200 =>
          successTimes.incrementAndGet() / checkTimes.get()
        case _ =>
          successTimes.get() / checkTimes.incrementAndGet()
      }
    }.recover {
      case NonFatal(_) =>
        successTimes.get() / checkTimes.incrementAndGet()
    }.get

    usability
  }
  
  override def hashCode(): Int = Objects.hash(this.host.asInstanceOf[Object], this.port.asInstanceOf[Object])

  override def equals(obj: scala.Any): Boolean = (obj) match {
    case t: ProxyDTO =>
      t.host == this.host && t.port == this.port
    case _ => false
  }

  override def toString: String = s"${host}:${port}"
}

object ProxyStatusEnums extends Enumeration {
  type ProxyStatusEnums = Value
  val USING = Value("using")
  val IDEL = Value("idel")
}
