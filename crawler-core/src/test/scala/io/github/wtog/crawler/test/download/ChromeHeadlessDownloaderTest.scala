package io.github.wtog.crawler.test.download

import io.github.wtog.crawler.downloader.ChromeHeadlessDownloader
import io.github.wtog.crawler.spider.Spider
import io.github.wtog.crawler.test.BaseCoreTest
import org.scalatest.BeforeAndAfter

/**
  * @author : tong.wang
  * @since : 1/12/20 10:48 PM
  * @version : 1.0.0
  */
class ChromeHeadlessDownloaderTest extends BaseCoreTest with BeforeAndAfter {

  after {
    ChromeHeadlessDownloader.closeClient()
  }

  lazy val localProcessor = LocalProcessor()

  ignore("chrome driver get xhr response") {
    val url = "https://flight.qunar.com/site/oneway_list.htm?searchDepartureAirport=%E5%8C%97%E4%BA%AC&searchArrivalAirport=%E6%88%90%E9%83%BD&searchDepartureTime=2020-03-11&searchArrivalTime=2020-03-12&nextNDays=0&startSearch=true&fromCode=BJS&toCode=CTU&from=qunarindex&lowestPrice=nul"

    val page = await(ChromeHeadlessDownloader.download(
      spider = Spider(pageProcessor = localProcessor),
      request = localProcessor.requestSetting.withUrl(url).withXhrRequests("https://flight.qunar.com/touch/api/domestic/wbdflightlist")
    ))

    assert(page.isDownloadSuccess)

    println(page.xhrResponses)
  }

  test("chrome driver on linux") {
    val page = await(ChromeHeadlessDownloader.download(
      spider = Spider(pageProcessor = localProcessor),
      request = localProcessor.requestSetting
    ))

    assert(page.isDownloadSuccess)
    println(page.source)
  }

}
