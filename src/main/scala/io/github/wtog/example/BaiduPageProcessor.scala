package io.github.wtog.example

import io.github.wtog.processor.{ Page, PageProcessor, RequestSetting }

/**
 * @author : tong.wang
 * @since : 5/16/18 11:42 PM
 * @version : 1.0.0
 */
final case class BaiduPageProcessor() extends PageProcessor {

  override def process(page: Page): Unit = {
    // 处理爬去结果
    page.addPageResultItem(Map("title" -> page.title))
    // 添加新的爬去连接
    //    page.addTargetRequest("http://www.baidu.com")
  }

  override def requestSetting: RequestSetting = {
    RequestSetting(
      domain = "www.baidu.com",
      headers = Map("Content-Type" -> "text/html; charset=GB2312"), useProxy = true)
  }

  override def targetUrls: List[String] = List("http://www.baidu.com")

  override def cronExpression: Option[String] = Some("*/30 * * ? * *")
}

