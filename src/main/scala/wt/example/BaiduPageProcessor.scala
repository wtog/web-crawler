package wt.example

import wt.downloader.RequestHeaders
import wt.processor.{Page, PageProcessor}

/**
  * @author : tong.wang
  * @since : 5/16/18 11:42 PM
  * @version : 1.0.0
  */
object BaiduPageProcessor extends PageProcessor {
  override def process(page: Page): Unit = {
//    println("processor " + page.sourceCode)
    val document = page.jsoupParser

    page.addPageResultItem(document.title())
    
    page.addTargetRequest("http://www.baidu.com")
  }

  override def requestHeaders: RequestHeaders = {
    RequestHeaders(domain = "www.baidu.com")
  }
}
