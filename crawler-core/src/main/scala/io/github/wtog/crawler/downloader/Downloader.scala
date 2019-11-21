package io.github.wtog.crawler.downloader

import java.util.concurrent.{ ConcurrentHashMap, Executors, TimeUnit }
import java.util.concurrent.atomic.AtomicInteger

import io.github.wtog.crawler.downloader.proxy.{ ProxyDTO, ProxyProvider }
import io.github.wtog.crawler.processor.{ Page, RequestSetting }
import io.github.wtog.crawler.spider.Spider
import io.github.wtog.utils.RetryUtils._
import io.github.wtog.utils.{ ConfigUtils, RetryInfo }
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{ Failure, Success, Try }
import java.util.concurrent.ScheduledFuture

/**
  * @author : tong.wang
  * @since : 5/16/18 9:56 PM
  * @version : 1.0.0
  */
trait Downloader[Driver] {
  protected lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val downloadRetryException: Seq[String] = ConfigUtils.getSeq[String]("crawler.download.retry.exception")

  protected val clientsPool = new ConcurrentHashMap[String, DownloaderClient[Driver]]()

  protected def doDownload(requestSetting: RequestSetting): Future[Page]

  protected def buildProxy[P](proxyDto: ProxyDTO)(buildProxy: ProxyDTO => P): P = buildProxy(proxyDto)

  protected def closeClient(): Unit

  protected def getOrCreateClient(requestSetting: RequestSetting): DownloaderClient[Driver]

  def getClient(domain: String): Option[DownloaderClient[Driver]] = Option(clientsPool.get(domain))

  /**
    * common schedule job to close download client
    */
  val scheduleClose: ScheduledFuture[_] = Executors
    .newSingleThreadScheduledExecutor()
    .scheduleAtFixedRate(new Runnable {
      override def run(): Unit =
        try (closeClient())
        catch { case e: Throwable => logger.error("", e) }
    }, 3, 3, TimeUnit.MINUTES)

  def download(spider: Spider, request: RequestSetting): Future[Page] = {
    import io.github.wtog.crawler.actor.ExecutionContexts.downloadDispatcher
    futureRetryWhen(doDownload(requestSetting = request), retryTime = request.retryTime, RetryInfo(duration = request.sleepTime, downloadRetryException))
      .map { page =>
        spider.CrawlMetric.record(page.isDownloadSuccess, page.url)
        page
      }
      .recover {
        case NonFatal(e) =>
          spider.CrawlMetric.record(success = false, request.url.get)
          throw e
      }
  }

  protected def executeRequest[HttpResponse](requestSetting: RequestSetting)(execute: Option[ProxyDTO] => Future[HttpResponse]): Future[HttpResponse] =
    if (requestSetting.useProxy) {
      execute(ProxyProvider.getProxy)
    } else {
      execute(None)
    }

  protected def pageResult(requestSetting: RequestSetting, results: Option[Array[Byte]] = None, downloadSuccess: Boolean = true, msg: Option[String] = None): Page = {
    if (!downloadSuccess) {
      logger.warn(s"failed to download ${requestSetting.url.get}, cause ${msg.getOrElse("")} ")
    }
    Page(downloadSuccess, bytes = results, requestSetting = requestSetting)
  }

  protected def getDownloaderClient(domain: String)(driver: => Driver): DownloaderClient[Driver] = {
    val clientCache = Option(clientsPool.get(domain))

    val downloaderClient = clientCache.getOrElse {
      val downloaderClient = DownloaderClient(domain = domain, driver = driver)
      clientsPool.put(domain, downloaderClient)
      downloaderClient
    }

    downloaderClient.increment()
    downloaderClient
  }

  def closeDownloaderClient(close: Driver => Unit): Unit = {
    import scala.collection.JavaConversions._
    for (e <- clientsPool.entrySet()) {
      val (domain, downloaderClient) = (e.getKey, e.getValue)
      if (downloaderClient.idle()) {
        Try(close(downloaderClient.driver)) match {
          case Success(_) =>
            logger.info(s"${domain} downloader driver[${downloaderClient.driver.getClass.getSimpleName}] has been closed.")
          case Failure(exception) =>
            logger.error(s"${domain} downloader driver failed to close. ${exception.getLocalizedMessage}")
        }
        clientsPool.remove(domain)
      }
    }
  }

  sys.addShutdownHook {
    try (closeClient())
    catch { case e: Throwable => logger.error("", e) }
  }
}

case class DownloaderClient[C](domain: String, driver: C, consumers: AtomicInteger = new AtomicInteger(0)) {
  def idle(): Boolean      = consumers.get() == 0
  def increment(): Int = consumers.incrementAndGet()
  def decrement(): Int = consumers.decrementAndGet()
}

object Downloader {

  object ContentType {
    lazy val FORM_URLENCODED: Map[String,String] = Map("Content-Type" -> "application/x-www-form-urlencoded")
    lazy val TEXT_PLAIN: Map[String,String]      = Map("Content-Type" -> "text/plain")
    lazy val TEXT_JSON: Map[String,String]       = Map("Content-Type" -> "application/json")
  }

}
