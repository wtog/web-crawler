package io.github.wtog

import java.util.concurrent.TimeUnit

import io.github.wtog.example.BaiduPageProcessor
import io.github.wtog.processor.impl.beijinghouse.BeijingHouseProcessor
import io.github.wtog.spider.Spider

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

  "crawlMetric" should "add" in {
    val spider = Spider(pageProcessor = BeijingHouseProcessor())
    spider.CrawlMetric.downloadFailedCounter
    spider.CrawlMetric.downloadFailedCounter

    println(spider.CrawlMetric.downloadedPageSum)
    spider.CrawlMetric.metricInfo()
  }

}
