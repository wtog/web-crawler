package io.github.wtog.test

import io.github.wtog.example.BaiduPageProcessor
import io.github.wtog.processor.RequestSetting
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * @author : tong.wang
  * @since : 5/16/18 9:19 PM
  * @version : 1.0.0
  */
trait BaseTest extends FunSuite with Matchers with BeforeAndAfter {

  before {
    System.getProperty("config.resource", "application-test.conf")
  }
  
  lazy val requestSettingTest = RequestSetting(
    domain = "www.baidu.com",
    url = Some("https://www.baidu.com/s?wd=wtog%20web-crawler")
  )

  case class TestProcessor(requestSettingTest: RequestSetting = requestSettingTest) extends BaiduPageProcessor {
    override def requestSetting = requestSettingTest
  }

  def await[T](future: => Future[T]) = Await.result(future, 1 minute)
}
