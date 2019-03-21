package io.github.wtog.downloader.proxy.crawler

import io.github.wtog.processor.{ Page, RequestSetting }
import scala.concurrent.duration._

/**
 * https://raw.githubusercontent.com/a2u/free-proxy-list/master/free-proxy-list.txt
 * @author : tong.wang
 * @since : 6/3/18 12:33 AM
 * @version : 1.0.0
 */
class A2UPageProcessor extends ProxyProcessorTrait {
  val targetUrl = List("https://raw.githubusercontent.com/a2u/free-proxy-list/master/free-proxy-list.txt")

  override def process(page: Page): Unit = {
    val proxyIpList = page.body.text().split(" ")

    proxyIpList.foreach(it ⇒ {
      val ipAndPort = it.split(":")
      var proxyIP: Map[String, Any] = Map()
      proxyIP += ("host" -> ipAndPort(0))
      proxyIP += ("port" -> ipAndPort(1))

      page.addPageResultItem(proxyIP)
    })
  }

  override def requestSetting: RequestSetting = RequestSetting(domain = "raw.githubusercontent.com", sleepTime = 3 seconds)

  override def targetUrls: List[String] = {
    List("https://raw.githubusercontent.com/a2u/free-proxy-list/master/free-proxy-list.txt")
  }

}
