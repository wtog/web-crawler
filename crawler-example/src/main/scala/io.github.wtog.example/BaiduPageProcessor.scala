package io.github.wtog.example

import io.github.wtog.crawler.processor.{ Page, PageProcessor, RequestSetting }

import scala.concurrent.duration._

/**
  * @author : tong.wang
  * @since : 5/16/18 11:42 PM
  * @version : 1.0.0
  */
class BaiduPageProcessor() extends PageProcessor {

  override def doProcess(page: Page): Unit = {
    val hotSearched = page.div("#content_right .opr-toplist1-table tr")

    val href    = hotSearched.select("td a").attr("href")
    val content = hotSearched.select("td a").text()
    val hot     = hotSearched.select("td").last().text()

    page.addPageResultItem(
      Map("href" -> href, "content" -> content, "hot" -> hot)
    )

  }

  override def requestSetting: RequestSetting =
    RequestSetting(
      domain = "www.baidu.com",
      headers = Map("Content-Type" -> "text/html; charset=GB2312"),
      sleepTime = 1 seconds
    )

  override def targetUrls: List[String] = List("https://www.baidu.com/s?wd=wtog%20web-crawler")

  override def cronExpression: Option[String] = Some("*/30 * * ? * *")
}
