package io.github.wtog.crawler.test.download

import io.github.wtog.crawler.downloader.{AsyncHttpClientDownloader, ChromeHeadlessDownloader}
import io.github.wtog.crawler.spider.Spider
import io.github.wtog.crawler.test.BaseCoreTest
import org.scalatest.BeforeAndAfter

/**
  * @author : tong.wang
  * @since : 5/20/18 11:22 AM
  * @version : 1.0.0
  */
class DownloaderTest extends BaseCoreTest with BeforeAndAfter {

  after {
    ChromeHeadlessDownloader.closeClient()
    AsyncHttpClientDownloader.closeClient()
  }

  test("retry download") {
    val requestSetting = requestSettingTest.copy(
      url = Some("http://top.baidu.com/buzz?b=1&c=513&fr=topbuzz_b344_c513"),
      retryTime = 3
    )

    val processor = TestProcessor(requestSetting)

    val page = await(AsyncHttpClientDownloader.download(Spider(pageProcessor = processor), requestSetting))

    assert(page.isDownloadSuccess)
  }

  val processor = LocalProcessor()
  val url = processor.requestSetting.url.get

  test("chrome driver close resource safely") {
    val page = await(ChromeHeadlessDownloader.download(Spider(pageProcessor = processor), request = processor.requestSetting.withUrl(s"${url}?id=0")))

    assert(page.isDownloadSuccess)
    assert(page.source.nonEmpty)

    ChromeHeadlessDownloader.closeClient()

    assert(ChromeHeadlessDownloader.getClient(processor.requestSetting.domain).isEmpty)
  }

  test("asynchttpclient driver close resource safely") {
    val page = await(AsyncHttpClientDownloader.download(Spider(pageProcessor = processor), request = processor.requestSetting.withUrl(s"${url}?id=0")))

    assert(page.isDownloadSuccess)
    assert(page.source.nonEmpty)
    AsyncHttpClientDownloader.closeClient()

    assert(AsyncHttpClientDownloader.getClient(processor.requestSetting.domain).isEmpty)
  }

}
