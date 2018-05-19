package wt.queue

import wt.downloader.RequestHeaderGeneral

/**
  * @author : tong.wang
  * @since : 5/16/18 10:03 PM
  * @version : 1.0.0
  */
trait RequestQueue {

  def push(request: RequestHeaderGeneral)

  def poll(): Option[RequestHeaderGeneral]

  def isEmpty: Boolean
}
