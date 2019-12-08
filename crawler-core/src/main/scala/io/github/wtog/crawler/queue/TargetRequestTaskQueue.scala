package io.github.wtog.crawler.queue

import java.util.concurrent.LinkedBlockingQueue

import io.github.wtog.crawler.processor.RequestSetting
import io.github.wtog.crawler.queue.duplicate.{ DuplicateRemovedStrategy, HashMapStrategy }

/**
  * @author : tong.wang
  * @since : 5/16/18 10:12 PM
  * @version : 1.0.0
  */
class TargetRequestTaskQueue(duplicateRemovedStrategy: DuplicateRemovedStrategy = HashMapStrategy) extends DuplicateRemovedQueue(duplicateRemovedStrategy) {
  private lazy val queue: LinkedBlockingQueue[RequestSetting] = new LinkedBlockingQueue[RequestSetting]()

  override def pushWhenNoDuplicate(request: RequestSetting): Unit = this.queue.add(request)

  override def isEmpty: Boolean = queue.isEmpty

  override def doPoll(): Option[RequestSetting] = Option(this.queue.poll())

}
