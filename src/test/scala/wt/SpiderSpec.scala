package wt

import java.util.concurrent.TimeUnit

import wt.example.BaiduPageProcessor
import wt.pipeline.ConsolePipeline

/**
  * @author : tong.wang
  * @since : 5/16/18 9:19 PM
  * @version : 1.0.0
  */
class SpiderSpec extends BaseTest {

  "spider 10s 1" should "crawl page size over 100" in {
    val pipelineList1 = List(ConsolePipeline)
    Spider(List("http://www.baidu.com"), pageProcessor = BaiduPageProcessor, pipelineList = pipelineList1).start()
    TimeUnit.SECONDS.sleep(5)
  }

  "spider 10s 2" should "crawl page size over 100" in {
    val pipelineList2 = List(ConsolePipeline)
    Spider(List("http://www.baidu.com"), pageProcessor = BaiduPageProcessor, pipelineList = pipelineList2).start()
    TimeUnit.SECONDS.sleep(5)
  }

}
