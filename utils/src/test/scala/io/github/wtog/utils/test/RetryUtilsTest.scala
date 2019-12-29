package io.github.wtog.utils.test

import java.util.concurrent.TimeUnit

import io.github.wtog.utils.RetryInfo
import io.github.wtog.utils.RetryUtils._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * @author : tong.wang
  * @since : 2019-05-07 23:35
  * @version : 1.0.0
  */
class RetryUtilsTest extends BaseTest {

  test("retry") {
    var invokeTime = 0

    def method(limit: Int): Int = {
      invokeTime += 1
      if (invokeTime < limit) throw new Exception(s"invokeTime: ${invokeTime}") else invokeTime
    }

    assert(Try(retryWhen(method(6), retryTime = 3, RetryInfo(exceptions = Seq(classOf[Exception].getName)))).isFailure)
    invokeTime = 0
    assert(3 == retryWhen(method(3),retryTime = 3, RetryInfo(exceptions = Seq(classOf[Exception].getName))))
    invokeTime = 0
    assert(2 == retryWhen(method(2),retryTime = 3,RetryInfo(exceptions = Seq(classOf[Exception].getName))))
    invokeTime = 0
    assert(2 == retryWhen(method(2),retryTime = 1, RetryInfo(exceptions = Seq(classOf[Exception].getName))))
    invokeTime = 0
    assert(Try(retryWhen(method(3), retryTime = 1, RetryInfo(exceptions = Seq(classOf[Exception].getName)))).isFailure)
  }

  test("futureRetryWhen") {

    import scala.concurrent.ExecutionContext.Implicits.global

    class Test(invokeTime: Int = 0) {
      var _invokeTime = invokeTime

      def method(limit: Int): Future[Int] = {
        _invokeTime += 1
        Future {
          if (_invokeTime < limit) throw new Exception(s"invokeTime: ${invokeTime}") else _invokeTime
        }(ExecutionContext.Implicits.global)
      }
    }


    val t1 = new Test()
    (futureRetryWhen(t1.method(6),retryTime = 3,  RetryInfo(exceptions = Seq(classOf[Exception].getName), duration = 10 millis))).onComplete(r => assert(r.isFailure))

    val t2 = new Test()
    (futureRetryWhen(t2.method(3),retryTime = 3,  RetryInfo(exceptions = Seq(classOf[Exception].getName),  duration = 10 millis))).onComplete(r => assert(r.isSuccess && r.get == 3))

    val t3 = new Test()
    (futureRetryWhen(t3.method(2),retryTime = 3,  RetryInfo(exceptions = Seq(classOf[Exception].getName),  duration = 10 millis))).onComplete(r => assert(r.isSuccess && r.get == 2))

    val t4 = new Test()
    (futureRetryWhen(t4.method(2),retryTime = 3,  RetryInfo(exceptions = Seq(classOf[Exception].getName),  duration = 10 millis))).onComplete(r => assert(r.isSuccess && r.get == 2))

    val t5 = new Test()
    (futureRetryWhen(t5.method(3),retryTime = 1,  RetryInfo(exceptions = Seq(classOf[Exception].getName),  duration = 10 millis))).onComplete(r => assert(r.isFailure))

    val t6 = new Test()
    (futureRetryWhen(t6.method(0).failed, retryTime = 3, RetryInfo(10 millis))).onComplete(r => assert(r.isFailure))

    TimeUnit.SECONDS.sleep(1)
  }
}
