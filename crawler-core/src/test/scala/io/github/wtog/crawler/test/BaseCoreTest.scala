package io.github.wtog.crawler.test

import java.util.concurrent.atomic.AtomicInteger

import io.github.wtog.crawler.dto.{Page, RequestSetting}
import io.github.wtog.crawler.processor.PageProcessor
import io.github.wtog.crawler.rest.Server
import io.github.wtog.crawler.test.server.TestMockServer
import io.github.wtog.utils.ConfigUtils
import io.github.wtog.utils.test.BaseTest

/**
  * @author : tong.wang
  * @since : 5/16/18 9:19 PM
  * @version : 1.0.0
  */
trait BaseCoreTest extends BaseTest {

  lazy val port = ConfigUtils.getIntOpt("crawler.server.port").getOrElse(19000)

  lazy val localServerHost = s"http://localhost:${port}"

  override def beforeAll() = {
    System.getProperty("config.resource", "application-test.conf")
    System.getProperty("log4j.resource", "log4j2-test.xml")
    if (!Server.running)
      TestMockServer.start
  }

  lazy val requestSettingTest = RequestSetting(
    domain = "www.baidu.com",
    url = Some("https://www.baidu.com/s?wd=wtog%20web-crawler")
  )

  case class LocalProcessor(requestSettingTest: Option[RequestSetting] = None) extends PageProcessor {

    val link = new AtomicInteger(0)

    override def targetUrls: List[String] = List(localServerHost)

    override protected def doProcess(page: Page): Unit = {
      assert(page.isDownloadSuccess)
      page.addTargetRequest(s"${page.url}?id=${link.incrementAndGet()}")
    }

    override def requestSetting: RequestSetting = {
      requestSettingTest.getOrElse(
      RequestSetting(
        domain = "localhost",
        url = Some(s"http://localhost:${port}/mock/get")
      ))
    }

    def getUrl(route: String): String = s"http://localhost:${port}${route}"
  }

  object LocalProcessor {
    def apply(): LocalProcessor = new LocalProcessor()

    def apply(requestSetting: Option[RequestSetting] = None): LocalProcessor = new LocalProcessor(requestSetting)

    def apply(requestSetting: RequestSetting): LocalProcessor = new LocalProcessor(Some(requestSetting))

  }

}
