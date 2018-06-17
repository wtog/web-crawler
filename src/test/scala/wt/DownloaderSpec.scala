package wt

import java.util.concurrent.TimeUnit

import wt.downloader.{ApacheHttpClientDownloader, RequestHeaderGeneral}
import wt.example.BaiduPageProcessor

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * @author : tong.wang
  * @since : 5/20/18 11:22 AM
  * @version : 1.0.0
  */
class DownloaderSpec extends BaseTest {

  "apacheHttpClient" should "work well" in {
    val response = ApacheHttpClientDownloader.download(BaiduPageProcessor().requestHeaders.copy(
      domain = "www.baidu.com", requestHeaderGeneral = Some(RequestHeaderGeneral(url = Some("http://top.baidu.com/buzz?b=1&c=513&fr=topbuzz_b344_c513")))))

    val r = Await.result(response, Duration(15, TimeUnit.SECONDS))
    println(r.pageSource.get)
  }
}
