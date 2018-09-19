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

    page.addPageResultItem(Map("title" -> document.title()))

    page.addTargetRequest("http://www.baidu.com")
  }

  override def requestHeaders: RequestHeaders = {
    RequestHeaders(
      domain = "www.baidu.com",
      headers = Some(Map("Content-Type" -> "text/html; charset=GB2312")), useProxy = true)
  }

  override def targetUrls: List[String] = {
    List("http://www.baidu.com")
  }
}
