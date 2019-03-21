package io.github.wtog

import java.util.concurrent.TimeUnit

import io.github.wtog.downloader.{ ApacheHttpClientDownloader, AsyncHttpClientDownloader }
import io.github.wtog.example.BaiduPageProcessor
import io.github.wtog.processor.Page

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

/**
 * @author : tong.wang
 * @since : 5/20/18 11:22 AM
 * @version : 1.0.0
 */
class DownloaderSpec extends BaseTest {

  test("apache httpclient and async httpclient comparison") {
    val processor = new BaiduPageProcessor().requestSetting.copy(domain = "top.baidu.com", url = Some("http://top.baidu.com/buzz?b=1&c=513&fr=topbuzz_b344_c513"))

    val requestTimes = 60
    lazy val apacheJob = () ⇒ ApacheHttpClientDownloader.download(processor)
    lazy val asyncJob = () ⇒ AsyncHttpClientDownloader.download(processor)

    lazy val async = download(asyncJob, requestTimes)
    lazy val apache = download(apacheJob, requestTimes)

    lazy val syncResult = getResult(apache)
    lazy val asyncResult = getResult(async)
    println(s"sync : ${syncResult.size} - ${syncResult.sorted}")
    println(s"async: ${asyncResult.size} - ${asyncResult.sorted}")
  }

  def getResult(result: Future[Seq[Long]]): Seq[Long] = {
    Await.result(result, 2 minutes)
  }

  def download(download: () ⇒ Future[Page], requestTimes: Int): Future[Seq[Long]] = {
    val r = (1 to requestTimes).map { _ ⇒
      TimeUnit.SECONDS.sleep(1)
      val begin = System.currentTimeMillis()
      download().map { _ ⇒
        val end = System.currentTimeMillis()
        end - begin
      }

    }

    Future.sequence(r)
  }
}
