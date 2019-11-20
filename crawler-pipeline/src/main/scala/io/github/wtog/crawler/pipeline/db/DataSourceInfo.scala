package io.github.wtog.crawler.pipeline.db

import scala.concurrent.duration._

/**
  * @author : tong.wang
  * @since : 10/28/19 11:32 PM
  * @version : 1.0.0
  */
case class DataSourceInfo(
    database: String = "default",
    jdbcUrl: String,
    username: String,
    password: String,
    maxPoolSize: Int = 10,
    minIdleSize: Int = 1,
    idleTimeout: Duration = 2 seconds)
