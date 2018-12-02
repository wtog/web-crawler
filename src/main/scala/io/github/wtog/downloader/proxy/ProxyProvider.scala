package io.github.wtog.downloader.proxy

import java.net.{ HttpURLConnection, InetSocketAddress, URL }
import java.util.Objects
import java.util.concurrent.{ ArrayBlockingQueue, Executors }
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicInteger }

import io.github.wtog.actor.ActorManager
import io.github.wtog.downloader.proxy.ProxyProvider.checkUrl
import io.github.wtog.downloader.proxy.ProxyStatusEnums.ProxyStatusEnums
import io.github.wtog.processor.PageProcessor
import io.github.wtog.spider.{ Spider, SpiderPool }
import io.github.wtog.utils.ClassUtils
import org.apache.http.client.methods.CloseableHttpResponse
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.ExecutionContext.Implicits.global
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
  val proxyList: ArrayBlockingQueue[ProxyDTO] = new ArrayBlockingQueue[ProxyDTO](100)
  private val proxySpiderCrawling: AtomicBoolean = new AtomicBoolean(false)
  private val checkThread = Executors.newFixedThreadPool(5)
  private val monitorProxyStatus = new AtomicBoolean(false)

  private lazy val proxyCrawlerList = ClassUtils.loadClasses("io.github.wtog.downloader.proxy.crawler", classOf[PageProcessor]).toList.map { proxy ⇒
    (Spider(name = s"proxy-${proxy.getClass.getSimpleName}", pageProcessor = proxy))
  }

  def listProxyStatus() = {
    if (!monitorProxyStatus.getAndSet(true)) {
      ActorManager.system.scheduler.schedule(5 seconds, 2 seconds)({
        if (logger.isDebugEnabled()) {
          logger.debug(s"proxy sum: ${proxyList.size}")
        }

        if (proxyList.size > 50 || SpiderPool.fetchAllUsingProxySpiders().length == 0) {
          proxyCrawlerList.foreach { _.stop() }
          proxySpiderCrawling.set(false)
        }

        if (proxyList.size < 25 && proxyCrawlerList.forall(!_.running.get())) {
          startProxyCrawl(restart = true)
        }
      })
    }
  }

  private def crawlCronJob(restart: Boolean = false) = {
    proxyCrawlerList.foreach { spider ⇒
      if (restart)
        spider.restart()
      else {
        spider.start()
      }
    }

    Option(ActorManager.system.scheduler.schedule(0 seconds, 2 seconds)({
      for (_ ← 1 to 5) {
        checkThread.execute(new Runnable {
          override def run(): Unit = {
            Option(proxyList.poll()) foreach { headProxy ⇒
              headProxy.usabilityCheck()
              if (headProxy.usability > 0.5 && headProxy.successTimes.get() < 2)
                proxyList.put(headProxy)
            }
          }
        })
      }
    }))
  }

  def startProxyCrawl(restart: Boolean = false) = {
    if (!proxySpiderCrawling.getAndSet(true)) {
      crawlCronJob(restart)
    }
  }

  def getProxy: Option[ProxyDTO] = {
    Option(proxyList.peek()).filter(_.usability > 0.5)
  }

  def requestWithProxy[T <: CloseableHttpResponse](useProxy: Boolean, httpRequest: Option[ProxyDTO] ⇒ T): T = {
    if (useProxy) {
      getProxy match {
        case proxy @ Some(p) ⇒
          try {
            p.status = ProxyStatusEnums.USING
            logger.info(s"using proxy ${p}")
            httpRequest(proxy)
          } catch {
            case NonFatal(e) ⇒
              logger.warn(s"failed to execute request using proxy: ${e.getLocalizedMessage}")
              Future {
                p.usabilityCheck()
              }
              httpRequest(None)
          } finally {
            p.status = ProxyStatusEnums.IDEL
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
    var usability: Float            = 0F) {

  val checkTimes: AtomicInteger = new AtomicInteger(0)
  val successTimes: AtomicInteger = new AtomicInteger(0)

  def usabilityCheck(): Float = {
    usability = Try {
      import java.net.Proxy
      val proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port))
      val connection = checkUrl.openConnection(proxy).asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(2000)
      connection.setReadTimeout(2000)

      val checkTimeValue = checkTimes.incrementAndGet()
      connection.getResponseCode match {
        case 200 ⇒
          successTimes.incrementAndGet() / checkTimeValue
        case _ ⇒
          successTimes.get() / checkTimeValue
      }
    }.recover {
      case NonFatal(_) ⇒ successTimes.get() / checkTimes.get()
    }.get

    usability
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
