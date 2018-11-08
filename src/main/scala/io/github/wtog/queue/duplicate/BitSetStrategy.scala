package io.github.wtog.queue.duplicate

import scala.collection.BitSet

/**
 * @author : tong.wang
 * @since : 11/8/18 1:31 PM
 * @version : 1.0.0
 */
object BitSetStrategy extends DuplicateRemovedStrategy {
  var urlBitSet = BitSet.empty

  override def isDuplicate(url: String): Boolean = {
    val urlHashCode = urlToHashCode(url)
    val isDuplicate = urlBitSet.contains(urlHashCode)

    if (!isDuplicate) {
      urlBitSet += urlHashCode
    }

    isDuplicate
  }

  def urlToHashCode(url: String): Int = {
    url.hashCode & 0x7FFFFFFF
  }
}
