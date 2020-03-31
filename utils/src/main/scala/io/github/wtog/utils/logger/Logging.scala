package io.github.wtog.utils.logger

import org.slf4j.{ Logger, LoggerFactory }

/**
  * @author : tong.wang
  * @since : 3/3/20 11:33 PM
  * @version : 1.0.0
  */
trait Logging {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)
}
