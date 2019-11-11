package io.github.wtog.utils.test

import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * @author : tong.wang
  * @since : 10/30/19 11:54 PM
  * @version : 1.0.0
  */
class BaseTest extends FunSuite  with Matchers with BeforeAndAfterAll{

  def await[T](future: => Future[T]) = Await.result(future, 1 minute)

}
