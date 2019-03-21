package io.github.wtog.test.proxy

import java.util.concurrent.TimeUnit

import io.github.wtog.downloader.proxy.ProxyProvider
import io.github.wtog.downloader.proxy.crawler.{A2UPageProcessor, Data5UPageProcessor}
import io.github.wtog.spider.Spider
import io.github.wtog.test.BaseTest

/**
  * @author : tong.wang
  * @since : 2019-05-14 22:37
  * @version : 1.0.0
  */
class ProxyProviderTest extends BaseTest {

  test("Spider use proxy with requestting useProxy=true") {
    Spider(name = this.getClass.getSimpleName, pageProcessor = TestProcessor(requestSettingTest.copy(useProxy = true))).start()

    TimeUnit.SECONDS.sleep(10)
    assert(ProxyProvider.proxySpiderCrawling.get())
    assert(ProxyProvider.proxyList.size() > 0)

  }

  test("a2u proxy") {
    Spider(name = "a2u-proxy", pageProcessor = A2UPageProcessor()).start()
    TimeUnit.SECONDS.sleep(10)
  }

  test("data5u proxy") {
    Spider(name = "data5u-proxy", pageProcessor = Data5UPageProcessor()).start()
    TimeUnit.SECONDS.sleep(10)
  }

}
