package io.github.wtog.queue

import io.github.wtog.processor.RequestHeaderGeneral
import io.github.wtog.queue.duplicate.DuplicateRemovedStrategy

/**
 * @author : tong.wang
 * @since : 5/16/18 10:07 PM
 * @version : 1.0.0
 */
abstract class DuplicateRemovedQueue(duplicateRemovedStrategy: DuplicateRemovedStrategy) extends RequestQueue {

  override def push(request: RequestHeaderGeneral): Unit = {
    if (isNotDuplicateRequest(request)) {
      pushWhenNoDuplicate(request)
    }
  }

  private def isNotDuplicateRequest(requestHeaderGeneral: RequestHeaderGeneral): Boolean = {
    requestHeaderGeneral.method match {
      case "GET" ⇒
        duplicateRemovedStrategy.isDuplicate(requestHeaderGeneral.url.get)
      case "POST" ⇒
        duplicateRemovedStrategy.isDuplicate(requestHeaderGeneral.url.get)
        true
      case other ⇒
        logger.warn(s"unknown request method type: ${other}")
        true
    }
  }

  protected def pushWhenNoDuplicate(request: RequestHeaderGeneral)
}
