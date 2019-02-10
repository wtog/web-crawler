package io.github.wtog

import io.github.wtog.downloader.{ ApacheHttpClientDownloader, AsyncHttpClientDownloader }
import io.github.wtog.example.BaiduPageProcessor
import io.github.wtog.processor.{ Page, RequestHeaderGeneral }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
/**
 * @author : tong.wang
 * @since : 5/20/18 11:22 AM
 * @version : 1.0.0
 */
class DownloaderSpec extends BaseTest {

  "http client" should "comparison" in {
    val processor = BaiduPageProcessor().requestHeaders.copy(domain = "top.baidu.com", requestHeaderGeneral = Some(RequestHeaderGeneral(url = Some("http://top.baidu.com/buzz?b=1&c=513&fr=topbuzz_b344_c513"))))

    val requestTimes = 50
    val apacheJob = () ⇒ ApacheHttpClientDownloader.download(processor)
    val asyncJob = () ⇒ AsyncHttpClientDownloader.download(processor)

    val async = download(asyncJob, requestTimes)
    val apache = download(apacheJob, requestTimes)

    val syncResult = getResult(apache)
    val asyncResult = getResult(async)
    println(s"sync : ${syncResult.size} - ${syncResult.sorted}")
    println(s"async: ${asyncResult.size} - ${asyncResult.sorted}")
  }

  def getResult(result: Future[Seq[Long]]): Seq[Long] = {
    Await.result(result, 10 seconds)
  }

  def download(download: () ⇒ Future[Page], requestTimes: Int): Future[Seq[Long]] = {
    val r = (1 to requestTimes).map { i ⇒
      val begin = System.currentTimeMillis()
      download().map { _ ⇒
        val end = System.currentTimeMillis()
        end - begin
      }
    }

    Future.sequence(r)
  }
}
