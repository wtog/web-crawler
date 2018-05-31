package wt.queue

import wt.downloader.RequestHeaderGeneral

/**
  * @author : tong.wang
  * @since : 5/16/18 10:07 PM
  * @version : 1.0.0
  */
abstract class DuplicateRemovedQueue extends RequestQueue {

  override def push(request: RequestHeaderGeneral): Unit = {
    pushWhenNoDuplicate(request)
  }

  protected def pushWhenNoDuplicate(request: RequestHeaderGeneral)
}
