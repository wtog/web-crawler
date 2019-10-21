package io.github.wtog.crawler.downloader.proxy

import java.net.{ HttpURLConnection, InetSocketAddress, URL }
import java.util.Objects
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicInteger }
import java.util.concurrent.{ ArrayBlockingQueue, Executors }

import io.github.wtog.crawler.downloader.proxy.ProxyProvider._
import io.github.wtog.crawler.downloader.proxy.ProxyStatusEnums.ProxyStatusEnums
import io.github.wtog.crawler.processor.PageProcessor
import io.github.wtog.crawler.schedule.{ ScheduleJob, ScheduleJobs }
import io.github.wtog.crawler.spider.Spider
import io.github.wtog.utils.ClassUtils
import org.quartz.{ Job, JobExecutionContext }
import org.slf4j.{ Logger, LoggerFactory }

import scala.util.Try

/**
  * @author : tong.wang
  * @since : 5/20/18 11:08 AM
  * @version : 1.0.0
  */
object ProxyProvider {
  lazy val logger: Logger = LoggerFactory.getLogger(ProxyProvider.getClass)

  val checkThread = Executors.newFixedThreadPool(5)

  val proxyList: ArrayBlockingQueue[ProxyDTO] = new ArrayBlockingQueue[ProxyDTO](100)

  val proxySpiderCrawling: AtomicBoolean = new AtomicBoolean(false)

  private lazy val proxyCrawlerList: Seq[Spider] = ClassUtils
    .loadClasses(
      classOf[PageProcessor],
      "io.github.wtog.crawler.downloader.proxy.crawler"
    )
    .map { proxy ⇒
      (Spider(
        name = s"proxy-${proxy.getClass.getSimpleName}",
        pageProcessor = proxy
      ))
    }

  private def crawlCronJob(restart: Boolean = false) =
    if (restart) proxyCrawlerList.foreach(_.restart())
    else proxyCrawlerList.foreach(_.start())

  def startProxyCrawl(restart: Boolean = false) =
    if (!proxySpiderCrawling.getAndSet(true)) {
      crawlCronJob(restart)
      ScheduleJobs.addJob(scheduleJob = ScheduleJob(jobName = "proxy-check", cronExpression = "*/2 * * ? * *", task = classOf[ProxyCheckScheduleJob]))
    }

  def getProxy: Option[ProxyDTO] = Option(proxyList.peek()).filter(_.usability > 0.5)
}

class ProxyCheckScheduleJob extends Job {

  override def execute(context: JobExecutionContext): Unit = {
    def checkProxy() = Option(proxyList.poll()).foreach { proxy ⇒
      proxy.usabilityCheck(proxy.connect2Baidu()) match {
        case (_, true) =>
          proxyList.put(proxy)
        case (_, _) =>
      }
    }

    if (logger.isDebugEnabled) {
      logger.debug(s"proxylist is ${proxyList.size()}")
    }

    (1 to 5).foreach { _ =>
      checkThread.execute(new Runnable {
        override def run(): Unit = checkProxy()
      })
    }
  }

}

final case class ProxyDTO(
    host: String,
    port: Int,
    username: Option[String] = None,
    password: Option[String] = None,
    var status: ProxyStatusEnums = ProxyStatusEnums.IDEL,
    var usability: Float = 0f) {

  val checkUrl: URL               = new URL("http://www.baidu.com")
  val checkTimes: AtomicInteger   = new AtomicInteger(0)
  val successTimes: AtomicInteger = new AtomicInteger(0)
  val usabilityLimit              = 0.5

  def connect2Baidu(): Boolean = {
    import java.net.Proxy
    Try {
      val proxy      = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port))
      val connection = checkUrl.openConnection(proxy).asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(1000)
      connection.setReadTimeout(1000)

      connection.getResponseCode == 200
    }.recover {
      case _: Throwable =>
        false
    }.get

  }

  def usabilityCheck(checkWay: => Boolean): (Float, Boolean) = {
    usability = {
      val checkTimeValue = checkTimes.incrementAndGet()
      if (checkWay)
        successTimes.incrementAndGet() / checkTimeValue
      else
        successTimes.get() / checkTimeValue
    }

    (usability, usability > usabilityLimit)
  }

  override def hashCode(): Int = Objects.hash(this.host.asInstanceOf[Object], this.port.asInstanceOf[Object])

  override def equals(obj: scala.Any): Boolean = obj match {
    case t: ProxyDTO ⇒
      t.host == this.host && t.port == this.port
    case _ ⇒
      false
  }

  override def toString: String = s"${host}:${port}"
}

object ProxyStatusEnums extends Enumeration {
  type ProxyStatusEnums = Value
  val USING = Value("using")
  val IDEL  = Value("idel")
}
