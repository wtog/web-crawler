package io.wt.queue.duplicate

/**
  * @author : tong.wang
  * @since : 6/1/18 11:59 PM
  * @version : 1.0.0
  */
object HashSetStrategy extends DuplicateRemovedStrategy {
  var urlSet: Set[String] = Set()

  override def isDuplicate(url: String): Boolean = {
    if (!urlSet.contains(url)) {
      urlSet += url
      true
    } else {
      false
    }
  }

}
