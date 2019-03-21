package io.github.wtog.test.jmh

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * @author : tong.wang
  * @since : 2019-03-31 01:21
  * @version : 1.0.0
  */
class FutureBenchmark {

  @Benchmark
  def syncMap(): Int =
    Await.result(Future {0} map { _ + 1}, 1 seconds)

  @Benchmark
  def syncFlatMap(): Int =
    Await.result(Future {0} flatMap { f => Future.successful(f + 1)}, 1 seconds)

  @Benchmark
  def map(): Unit =
    Await.result(Future {0} map { _ + 1}, 1 seconds)

  @Benchmark
  def flatMap(): Unit =
    Await.result(Future {0} flatMap { f => Future.successful(f + 1)}, 1 seconds)

  @Benchmark
  def maphole(bh: Blackhole): Unit =
    bh.consume(Await.result(Future {0} map { _ + 1}, 1 seconds))

  @Benchmark
  def flatMaphole(bh: Blackhole): Unit =
    bh.consume(Await.result(Future {0} flatMap { f => Future.successful(f + 1)}, 1 seconds))

}

