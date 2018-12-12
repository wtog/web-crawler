package io.github.wtog.example

import io.github.wtog.processor.{ Page, PageProcessor, RequestHeaders }

/**
 * @author : tong.wang
 * @since : 5/16/18 11:42 PM
 * @version : 1.0.0
 */
final case class BaiduPageProcessor() extends PageProcessor {

  override def process(page: Page): Unit = {
    val document = page.jsoupParser

    // 处理爬去结果
    page.addPageResultItem(Map("title" -> document.title()))
    // 添加新的爬去连接
    //    page.addTargetRequest("http://www.baidu.com")
  }

  override def requestHeaders: RequestHeaders = {
    RequestHeaders(
      domain = "www.baidu.com",
      commonHeaders = Map("Content-Type" -> "text/html; charset=GB2312"), useProxy = true)
  }

  override def targetUrls: List[String] = List("http://www.baidu.com")

  override def cronExpression: Option[String] = Some("*/30 * * ? * *")
}

