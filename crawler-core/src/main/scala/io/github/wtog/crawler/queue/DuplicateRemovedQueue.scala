package io.github.wtog.crawler.queue

import io.github.wtog.crawler.processor.RequestSetting
import io.github.wtog.crawler.queue.duplicate.DuplicateRemovedStrategy

/**
  * @author : tong.wang
  * @since : 5/16/18 10:07 PM
  * @version : 1.0.0
  */
abstract class DuplicateRemovedQueue(duplicateRemovedStrategy: DuplicateRemovedStrategy) extends RequestQueue {

  override def push(request: RequestSetting): Unit =
    if (isNotDuplicateRequest(request)) {
      pushWhenNoDuplicate(request)
    }

  private def isNotDuplicateRequest(requestHeaderGeneral: RequestSetting): Boolean =
    requestHeaderGeneral.method match {
      case "GET" ⇒
        !duplicateRemovedStrategy.isDuplicate(requestHeaderGeneral.url.get)
      case "POST" ⇒
        !duplicateRemovedStrategy.isDuplicate(requestHeaderGeneral.url.get + requestHeaderGeneral.requestBody
            .getOrElse("")
        )
      case other ⇒
        logger.warn(s"unknown request method type: ${other}")
        true
    }

  protected def pushWhenNoDuplicate(request: RequestSetting): Unit
}
