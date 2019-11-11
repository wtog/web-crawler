package io.github.wtog.crawler.pipeline.test

import io.github.wtog.utils.test.BaseTest

/**
  * @author : tong.wang
  * @since : 10/30/19 11:43 PM
  * @version : 1.0.0
  */
trait BasePipelineTest extends BaseTest {

  protected def init()

  protected def cleanup()

  override def beforeAll(): Unit = init()

  override def afterAll(): Unit = cleanup()
}
