package io.github.wtog.downloader.proxy.crawler

import io.github.wtog.downloader.proxy.ProxyDTO
import io.github.wtog.processor.{ Page, RequestSetting }

import scala.concurrent.duration._
import scala.util.Try

/**
  * http://www.89ip.cn
  * @author : tong.wang
  * @since : 6/3/18 12:33 AM
  * @version : 1.0.0
  */
case class IP89Processor() extends ProxyProcessorTrait {
  override def doProcess(page: Page): Unit = {
    val iprows = page.table("tbody tr")
    iprows.foreach { ip =>
      val tds     = ip.select("td")
      val proxDto = ProxyDTO(host = tds.get(0).text(), port = Try(tds.get(1).text().toInt).getOrElse(80))
      page.addPageResultItem(proxDto)
    }
  }

  override def requestSetting: RequestSetting = RequestSetting(domain = "www.89ip.com", sleepTime = 2 seconds)

  override def targetUrls: List[String] = List("http://www.89ip.cn")

}
