package wt.queue

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import wt.downloader.RequestHeaderGeneral
import wt.queue.duplicate.{DuplicateRemovedStrategy, HashSetStrategy}

/**
  * @author : tong.wang
  * @since : 5/16/18 10:12 PM
  * @version : 1.0.0
  */
class LinkQueue(duplicateRemovedStrategy: DuplicateRemovedStrategy = HashSetStrategy) extends DuplicateRemovedQueue(duplicateRemovedStrategy) {
  private lazy val queue: BlockingQueue[RequestHeaderGeneral] = new LinkedBlockingQueue

  override def poll(): Option[RequestHeaderGeneral] = {
    Option(this.queue.poll())
  }

  override def pushWhenNoDuplicate(request: RequestHeaderGeneral): Unit = {
    this.queue.add(request)
  }

  override def isEmpty: Boolean = queue.isEmpty
}
