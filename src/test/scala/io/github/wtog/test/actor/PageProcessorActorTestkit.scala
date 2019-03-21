package io.github.wtog.test.actor

import java.util.concurrent.TimeUnit

import akka.actor.Props
import akka.testkit.TestProbe
import io.github.wtog.dto.{DownloadEvent, ProcessorEvent}
import io.github.wtog.processor._
import io.github.wtog.spider.Spider

import scala.concurrent.duration._

/**
  * @author : tong.wang
  * @since : 2019-04-19 22:52
  * @version : 1.0.0
  */
class PageProcessorActorTestkit extends ActorTestBase {

  private class TestProcessor extends PageProcessor {

    override def targetUrls: List[String] = List("http://test")

    override protected def doProcess(page: Page): Unit = {
      (1 to 10).foreach(i => page.addTargetRequest(s"${page.url}/$i"))
    }

    override def requestSetting: RequestSetting = RequestSetting()
  }

  "pageProcessor" must {
    "send" in {
      val pageProcessorActorRevicer = system.actorOf(props = Props[PageProcessorActorReceiver])
      val testProb = new TestProbe(system)

      val testProcessor = new TestProcessor()
      val pages = testProcessor.targetUrls.map { url =>
        Page(bytes = Some("hh".getBytes()), requestSetting = testProcessor.requestSetting.withUrl(url))
      }

      val spider = Spider(pageProcessor = testProcessor)
      pages.foreach { p =>
        testProb.send(pageProcessorActorRevicer, ProcessorEvent(spider, p))
      }

      (1 to 10).foreach{i =>
        TimeUnit.SECONDS.sleep(1)
        testProb.expectMsg(2 seconds, DownloadEvent(spider, RequestSetting(url = Some(s"http://test/$i"))))}
    }

  }
}
