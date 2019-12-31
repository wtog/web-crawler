package io.github.wtog.crawler.test.download

import com.google.common.net.{HttpHeaders, MediaType}
import io.github.wtog.crawler.downloader.{AsyncHttpClientDownloader, ChromeHeadlessDownloader}
import io.github.wtog.crawler.processor.RequestUri
import io.github.wtog.crawler.spider.Spider
import io.github.wtog.crawler.test.BaseCoreTest
import io.github.wtog.crawler.test.server.TestPostMockRoute
import io.github.wtog.utils.JsonUtils
import io.netty.handler.codec.http.HttpHeaderNames
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

  lazy val url = LocalProcessor.requestSetting.url.get

  test("chrome driver close resource safely") {
    val page = await(ChromeHeadlessDownloader.download(Spider(pageProcessor = LocalProcessor), request = LocalProcessor.requestSetting.withUrl(s"${url}?id=0")))

    assert(page.isDownloadSuccess)
    assert(page.source.nonEmpty)

    ChromeHeadlessDownloader.closeClient()

    assert(ChromeHeadlessDownloader.getClient(LocalProcessor.requestSetting.domain).isEmpty)
  }

  test("asynchttpclient driver close resource safely") {
    val page = await(AsyncHttpClientDownloader.download(Spider(pageProcessor = LocalProcessor), request = LocalProcessor.requestSetting.withUrl(s"${url}?id=0")))

    assert(page.isDownloadSuccess)
    assert(page.source.nonEmpty)
    AsyncHttpClientDownloader.closeClient()

    assert(AsyncHttpClientDownloader.getClient(LocalProcessor.requestSetting.domain).isEmpty)
  }

  test("async http client post download") {
    val request = RequestUri(
      url = LocalProcessor.getUrl(TestPostMockRoute.route),
      method = TestPostMockRoute.method,
      requestBody = Some(JsonUtils.toJson(Map("a" -> "b"))),
      headers = Some(Map(HttpHeaders.CONTENT_TYPE -> MediaType.FORM_DATA.toString))
    )
    val page = await(AsyncHttpClientDownloader.download(Spider(pageProcessor = LocalProcessor), request = LocalProcessor.requestSetting.withRequestUri(request)))

    assert(page.isDownloadSuccess)
    assert(page.source.nonEmpty)

    AsyncHttpClientDownloader.closeClient()
  }
}
