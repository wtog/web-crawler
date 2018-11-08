package io.github.wtog.queue.duplicate

/**
 * @author : tong.wang
 * @since : 6/1/18 11:59 PM
 * @version : 1.0.0
 */
object HashSetStrategy extends DuplicateRemovedStrategy {
  var urlSet: Set[Int] = Set()

  override def isDuplicate(url: String): Boolean = {
    val urlHashCode = url.hashCode
    val isDuplicate = urlSet.contains(urlHashCode)
    if (!isDuplicate) {
      urlSet += url.hashCode
    }
    println(urlSet.size)
    isDuplicate
  }

}
