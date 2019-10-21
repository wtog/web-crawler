package io.github.wtog.crawler.downloader.proxy.crawler

import io.github.wtog.crawler.downloader.proxy.ProxyDTO
import io.github.wtog.crawler.processor.{ Page, RequestSetting }

import scala.concurrent.duration._

/**
  * https://raw.githubusercontent.com/a2u/free-proxy-list/master/free-proxy-list.txt
  * @author : tong.wang
  * @since : 6/3/18 12:33 AM
  * @version : 1.0.0
  */
case class A2UPageProcessor() extends ProxyProcessorTrait {
  override def doProcess(page: Page): Unit = {
    val proxyIpList = page.body.text().split(" ")

    proxyIpList.foreach(it ⇒ {
      val ipAndPort = it.split(":")
      val proxy     = ProxyDTO(ipAndPort.head, ipAndPort.last.toInt)
      page.addPageResultItem(proxy)
    })
  }

  override def requestSetting: RequestSetting = RequestSetting(domain = "raw.githubusercontent.com", sleepTime = 2 seconds)

  override def targetUrls: List[String] =
    List(
      "https://raw.githubusercontent.com/a2u/free-proxy-list/master/free-proxy-list.txt"
    )

}
