package io.github.wtog.crawler.test.queue

import io.github.wtog.crawler.queue.duplicate.{BitSetStrategy, HashMapStrategy}
import org.scalatest.FunSuite

/**
  * @author : tong.wang
  * @since : 12/8/19 12:27 AM
  * @version : 1.0.0
  */
class DuplicateStrategyTest extends FunSuite {
  val urls = Seq("url1", "url1", "url2")

  
  private def removeDuplicated(isDuplicate: String => Boolean): Seq[String] = {
    urls.collect { case x if (!isDuplicate(x)) => x }
  }

  test("hashMap remove duplicate") {
    assert(urls.distinct == removeDuplicated(HashMapStrategy.isDuplicate))
  }

  test("bitset remove duplicate") {
    assert(urls.distinct == removeDuplicated(BitSetStrategy.isDuplicate))
  }
}
