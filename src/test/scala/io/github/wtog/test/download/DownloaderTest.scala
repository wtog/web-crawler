package io.github.wtog.test.download

import io.github.wtog.downloader.{AsyncHttpClientDownloader, ChromeHeadlessDownloader}
import io.github.wtog.spider.Spider
import io.github.wtog.test.BaseTest

/**
  * @author : tong.wang
  * @since : 5/20/18 11:22 AM
  * @version : 1.0.0
  */
class DownloaderTest extends BaseTest {

  test("retry download") {
    val requestSetting = requestSettingTest.copy(
      url = Some("http://top.baidu.com/buzz?b=1&c=513&fr=topbuzz_b344_c513"),
      retryTime = 3
    )

    val processor = TestProcessor(requestSetting)

    val page = await(AsyncHttpClientDownloader.download(Spider(pageProcessor = processor), requestSetting))

    assert(page.isDownloadSuccess)
  }

  val procssor = LocalProcessor()
  val url = procssor.requestSetting.url.get

  test("chrome driver close resource safely") {
    val page = await(ChromeHeadlessDownloader.download(Spider(pageProcessor = procssor), request = procssor.requestSetting.withUrl(s"${url}?id=0")))

    assert(page.isDownloadSuccess)
    assert(page.source.nonEmpty)

    ChromeHeadlessDownloader.closeClient()

    assert(ChromeHeadlessDownloader.getClient(procssor.requestSetting.domain).isEmpty)
  }

  test("asynchttpclient driver close resource safely") {
    val page = await(AsyncHttpClientDownloader.download(Spider(pageProcessor = procssor), request = procssor.requestSetting.withUrl(s"${url}?id=0")))

    assert(page.isDownloadSuccess)
    assert(page.source.nonEmpty)
    AsyncHttpClientDownloader.closeClient()

    assert(AsyncHttpClientDownloader.getClient(procssor.requestSetting.domain).isEmpty)
  }

}
