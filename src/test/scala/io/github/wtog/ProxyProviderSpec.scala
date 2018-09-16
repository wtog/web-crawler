package io.github.wtog

import java.util.concurrent.TimeUnit

import io.github.wtog.downloader.proxy.ProxyProvider

/**
 * @author : tong.wang
 * @since : 6/3/18 2:54 PM
 * @version : 1.0.0
 */
class ProxyProviderSpec extends BaseTest {

  "proxyProvider" should "get proxyDto" in {
    (1 to 10) foreach { it ⇒
      ProxyProvider.getProxy.foreach(
        println(_))
      TimeUnit.SECONDS.sleep(it)
    }

  }

}
