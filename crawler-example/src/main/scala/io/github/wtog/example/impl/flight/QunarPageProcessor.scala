package io.github.wtog.example.impl.flight

import io.github.wtog.crawler.downloader.{ ChromeHeadlessDownloader, Downloader }
import io.github.wtog.crawler.dto.{ Page, RequestSetting, RequestUri }
import io.github.wtog.example.ExampleTrait

import scala.concurrent.duration._

/**
  * @author : tong.wang
  * @since : 1/14/20 8:41 PM
  * @version : 1.0.0
  */
class QunarPageProcessor extends ExampleTrait {

  override def targetRequests: List[RequestUri] = List(
    RequestUri("https://flight.qunar.com/site/oneway_list.htm?searchDepartureAirport=%E5%8C%97%E4%BA%AC&searchArrivalAirport=%E6%88%90%E9%83%BD&searchDepartureTime=2020-01-11&searchArrivalTime=2020-01-15&nextNDays=0&startSearch=true&fromCode=BJS&toCode=CTU&from=qunarindex&lowestPrice=null")
  )

  override def requestSetting: RequestSetting = RequestSetting(
    domain = "flight.qunar.com",
    sleepTime = 1 seconds,
    xhrRequests = Set("https://flight.qunar.com/touch/api/domestic/wbdflightlist")
  )

  override protected def doProcess(page: Page): Unit =
    page.xhrResponses.foreach { response =>
      println(response.result)
    }

  override val downloader: Downloader[_] = ChromeHeadlessDownloader
}
