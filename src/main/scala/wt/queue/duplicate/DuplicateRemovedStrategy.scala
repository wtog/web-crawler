package wt.queue.duplicate

/**
  * @author : tong.wang
  * @since : 6/1/18 8:44 AM
  * @version : 1.0.0
  */
trait DuplicateRemovedStrategy {
  def isDuplicate(url: String): Boolean

}
