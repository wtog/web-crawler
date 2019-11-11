package io.github.wtog.crawler.pipeline.test

import io.github.wtog.crawler.pipeline.db.DataSource

/**
  * @author : tong.wang
  * @since : 10/30/19 11:50 PM
  * @version : 1.0.0
  */
class DataSourceTest extends BasePipelineTest {
  object PgDataSource extends DataSource {
    override protected val driverClass: String = "org.postgresql.Driver"
  }

  test("pg process") {

    val datas = Map[String, Any](
      "a" -> 1,
      "b" -> 2
    )
  }

  override protected def init(): Unit = {}

  override protected def cleanup(): Unit = {}

}
