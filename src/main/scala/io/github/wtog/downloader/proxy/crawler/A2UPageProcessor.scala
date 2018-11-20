package io.github.wtog.downloader.proxy.crawler

import io.github.wtog.processor.{ Page, RequestHeaders }

/**
 * https://raw.githubusercontent.com/a2u/free-proxy-list/master/free-proxy-list.txt
 * @author : tong.wang
 * @since : 6/3/18 12:33 AM
 * @version : 1.0.0
 */
class A2UPageProcessor extends ProxyProcessorTrait {
  val targetUrl = List("https://raw.githubusercontent.com/a2u/free-proxy-list/master/free-proxy-list.txt")

  override def process(page: Page): Unit = {
    val document = page.jsoupParser
    val proxyIpList = document.text().split(" ")

    proxyIpList.foreach(it ⇒ {
      val ipAndPort = it.split(":")
      var proxyIP: Map[String, Any] = Map()
      proxyIP += ("host" -> ipAndPort(0))
      proxyIP += ("port" -> ipAndPort(1))

      page.addPageResultItem(proxyIP)
    })
  }

  override def requestHeaders: RequestHeaders = {
    RequestHeaders(domain = "raw.githubusercontent.com", sleepTime = 5000)
  }

  override def targetUrls: List[String] = {
    List("https://raw.githubusercontent.com/a2u/free-proxy-list/master/free-proxy-list.txt")
  }

}
