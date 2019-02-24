package io.github.wtog.queue

import java.util.concurrent.LinkedBlockingQueue

import io.github.wtog.processor.RequestSetting
import io.github.wtog.queue.duplicate.{ DuplicateRemovedStrategy, HashSetStrategy }

/**
 * @author : tong.wang
 * @since : 5/16/18 10:12 PM
 * @version : 1.0.0
 */
class TargetRequestTaskQueue(duplicateRemovedStrategy: DuplicateRemovedStrategy = HashSetStrategy) extends DuplicateRemovedQueue(duplicateRemovedStrategy) {
  private lazy val queue: LinkedBlockingQueue[RequestSetting] = new LinkedBlockingQueue[RequestSetting]()

  override def poll(): Option[RequestSetting] = Option(this.queue.poll())

  override def pushWhenNoDuplicate(request: RequestSetting): Unit = this.queue.add(request)

  override def isEmpty: Boolean = queue.isEmpty

  override def take(): Option[RequestSetting] = Option(this.queue.take())
}
