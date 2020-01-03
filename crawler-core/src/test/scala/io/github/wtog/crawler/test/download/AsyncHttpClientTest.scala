package io.github.wtog.crawler.test.download

import com.google.common.net.{HttpHeaders, MediaType}
import io.github.wtog.crawler.downloader.AsyncHttpClientDownloader
import io.github.wtog.crawler.dto.RequestUri
import io.github.wtog.crawler.spider.Spider
import io.github.wtog.crawler.test.BaseCoreTest
import io.github.wtog.crawler.test.server.TestPostMockRoute
import io.github.wtog.utils.JsonUtils
import org.scalatest.BeforeAndAfter

/**
  * @author : tong.wang
  * @since : 5/20/18 11:22 AM
  * @version : 1.0.0
  */
class AsyncHttpClientTest extends BaseCoreTest with BeforeAndAfter {

  after {
    AsyncHttpClientDownloader.closeClient()
  }

  lazy val localProcessor = LocalProcessor()
  lazy val url = localProcessor.requestSetting.url.get

  test("asynchttpclient driver close resource safely") {
    val page = await(AsyncHttpClientDownloader.download(Spider(pageProcessor = localProcessor), request = localProcessor.requestSetting.withUrl(s"${url}?id=0")))

    assert(page.isDownloadSuccess)
    assert(page.source.nonEmpty)
    AsyncHttpClientDownloader.closeClient()

    assert(AsyncHttpClientDownloader.getClient(localProcessor.requestSetting.domain).isEmpty)
  }

  test("async http client post download") {
    val request = RequestUri(
      url = localProcessor.getUrl(TestPostMockRoute.route),
      method = TestPostMockRoute.method,
      requestBody = Some(JsonUtils.toJson(Map("a" -> "b"))),
      headers = Some(Map(HttpHeaders.CONTENT_TYPE -> MediaType.FORM_DATA.toString))
    )
    val page = await(AsyncHttpClientDownloader.download(Spider(pageProcessor = localProcessor), request = localProcessor.requestSetting.withRequestUri(request)))

    assert(page.isDownloadSuccess)
    assert(page.source.nonEmpty)

    AsyncHttpClientDownloader.closeClient()
  }
}
