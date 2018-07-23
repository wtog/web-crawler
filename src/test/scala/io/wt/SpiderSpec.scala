package io.wt

import java.util.concurrent.TimeUnit

import io.wt.example.BaiduPageProcessor
import io.wt.pipeline.ConsolePipeline

/**
  * @author : tong.wang
  * @since : 5/16/18 9:19 PM
  * @version : 1.0.0
  */
class SpiderSpec extends BaseTest {

  "spider 10s 1" should "crawl page size over 100" in {
    Spider(pageProcessor = BaiduPageProcessor()).start()
    TimeUnit.SECONDS.sleep(5)
  }

  "spider 10s 2" should "crawl page size over 100" in {
    Spider(pageProcessor = BaiduPageProcessor()).start()
    TimeUnit.SECONDS.sleep(5)
  }

}
