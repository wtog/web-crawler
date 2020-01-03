package io.github.wtog.crawler.test.proxy

import java.util.concurrent.TimeUnit

import io.github.wtog.crawler.downloader.proxy.ProxyProvider
import io.github.wtog.crawler.downloader.proxy.crawler.{A2UPageProcessor, Data5UPageProcessor, IP89Processor}
import io.github.wtog.crawler.dto.RequestSetting
import io.github.wtog.crawler.spider.Spider
import io.github.wtog.crawler.test.BaseCoreTest

/**
  * @author : tong.wang
  * @since : 2019-05-14 22:37
  * @version : 1.0.0
  */
class ProxyProviderTest extends BaseCoreTest {

  test("spider use proxy with request setting useProxy=true") {
    val request = RequestSetting(
      url = Some(localServerHost),
      useProxy = true
    )

    Spider(pageProcessor = LocalProcessor(request)).start()
    assert(ProxyProvider.proxySpiderCrawling.get())
  }

  ignore("a2u proxy") {
    Spider(pageProcessor = A2UPageProcessor()).start()
    TimeUnit.SECONDS.sleep(10)
  }

  ignore("data5u proxy") {
    Spider(pageProcessor = Data5UPageProcessor()).start()
    TimeUnit.SECONDS.sleep(10)
  }

  ignore("ip89 proxy") {
    Spider(pageProcessor = IP89Processor()).start()
    TimeUnit.SECONDS.sleep(10)
  }
}
