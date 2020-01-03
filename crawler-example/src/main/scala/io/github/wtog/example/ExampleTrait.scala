package io.github.wtog.example

import io.github.wtog.crawler.processor.PageProcessor
import io.github.wtog.utils.ConfigUtils

/**
  * @author : tong.wang
  * @since : 1/14/20 8:31 PM
  * @version : 1.0.0
  */
trait ExampleTrait extends PageProcessor {

  val enable: Boolean = ConfigUtils.getBooleanOpt(s"crawler-examples.${this.getClass.getSimpleName}.enable").getOrElse(false)

}
