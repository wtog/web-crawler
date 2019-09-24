package io.github.wtog.test.download

import io.github.wtog.downloader.{AsyncHttpClientDownloader, ChromeHeadlessDownloader}
import io.github.wtog.spider.Spider
import io.github.wtog.test.BaseTest
import io.github.wtog.selector.HtmlParser._
import org.scalatest.BeforeAndAfter

/**
  * @author : tong.wang
  * @since : 5/20/18 11:22 AM
  * @version : 1.0.0
  */
class DownloaderTest extends BaseTest with BeforeAndAfter {

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

  val procssor = LocalProcessor()
  val url = procssor.requestSetting.url.get

  test("chrome driver close resource safely") {
//    val page = await(ChromeHeadlessDownloader.download(Spider(pageProcessor = procssor), request = procssor.requestSetting.withUrl(s"${url}?id=0")))
    val page = await(ChromeHeadlessDownloader.download(Spider(pageProcessor = procssor), request = procssor.requestSetting.withUrl(s"http://www.baidu.com")))

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

  test("asynchttpclient request https resource") {
    val url =
      """
        |https://www.zhihu.com/api/v4/members/rednaxelafx/answers?include=data%5B*%5D.is_normal%2Cadmin_closed_comment%2Creward_info%2Cis_collapsed%2Cannotation_action%2Cannotation_detail%2Ccollapse_reason%2Ccollapsed_by%2Csuggest_edit%2Ccomment_count%2Ccan_comment%2Ccontent%2Cvoteup_count%2Creshipment_settings%2Ccomment_permission%2Cmark_infos%2Ccreated_time%2Cupdated_time%2Creview_info%2Cquestion%2Cexcerpt%2Cis_labeled%2Clabel_info%2Crelationship.is_authorized%2Cvoting%2Cis_author%2Cis_thanked%2Cis_nothelp%2Cis_recognized%3Bdata%5B*%5D.author.badge%5B%3F(type%3Dbest_answerer)%5D.topics&offset=0&limit=10&sort_by=created
        |""".stripMargin

    val page = await(AsyncHttpClientDownloader.download(Spider(pageProcessor = procssor), request = procssor.requestSetting.withUrl(url)))

    assert(page.isDownloadSuccess)
    assert(page.source.nonEmpty)
    assert(page.json[Map[String, Any]]().get("data").isDefined)
    AsyncHttpClientDownloader.closeClient()
  }
}
