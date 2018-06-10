package wt.downloader.proxy

import java.net.{HttpURLConnection, InetSocketAddress, URL}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.slf4j.{Logger, LoggerFactory}
import wt.Spider
import wt.actor.ActorManager
import wt.downloader.proxy.ProxyProvider.checkUrl
import wt.downloader.proxy.ProxyStatusEnums.ProxyStatusEnums
import wt.downloader.proxy.crawler.{A2UPageProcessor, Data5UPageProcessor}

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
    ActorManager.system.scheduler.schedule(5 seconds, 5 seconds, new Runnable {
      override def run(): Unit = {
        proxyList.foreach { proxy: ProxyDTO =>
          logger.info(s"host: ${proxy.host}, status: ${proxy.status} usability: ${proxy.usability}") }
      }
    })
  }

  val proxyCrawlerList = {
    List((Spider(A2UPageProcessor.targetUrl, pageProcessor = A2UPageProcessor, pipelineList = List(ProxyCrawlerPipeline)), 30 seconds),
         (Spider(Data5UPageProcessor.targetUrl, pageProcessor = Data5UPageProcessor, pipelineList = List(ProxyCrawlerPipeline)), 30 seconds))
  }

  def getProxy: Option[ProxyDTO] = {
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

    if (proxyList.nonEmpty) {
      val chosen = proxyList.filter(_.status == ProxyStatusEnums.IDEL).find(it => {
        val usability = it.usabilityCheck() > 0.5

        if (it.checkTimes.get() > 10 && !usability) {
          proxyList -= it
        }
          
        usability
      })
      
      chosen match {
        case proxyDto @ Some(p) =>
          p.status = ProxyStatusEnums.USING
          proxyDto
        case none @ None =>
          none
      }
    } else {
      None
    }
  }

  def requestWithProxy[T <: Any](useProxy: Boolean, httpRequest: Option[ProxyDTO] => T): T = {
    import wt.actor.ExecutionContexts.downloadDispatcher
    if (useProxy) {
      getProxy match {
        case Some(p) =>
          p.status = ProxyStatusEnums.USING
          try {
            val proxyRequest = httpRequest(getProxy)
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
                    username: String,
                    password: String,
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
}

object ProxyStatusEnums extends Enumeration {
  type ProxyStatusEnums = Value
  val USING = Value("using")
  val IDEL = Value("idel")
}
