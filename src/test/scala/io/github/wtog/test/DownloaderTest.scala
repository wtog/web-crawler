package io.github.wtog.test

import io.github.wtog.downloader.AsyncHttpClientDownloader
import io.github.wtog.spider.Spider

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
}
