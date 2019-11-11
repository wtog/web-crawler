package io.github.wtog.crawler.pipeline.db

import io.github.wtog.crawler.pipeline.Pipeline

/**
  * @author : tong.wang
  * @since : 10/28/19 11:29 PM
  * @version : 1.0.0
  */
case class PostgreSQLPipeline(dataSouceInfo: DataSourceInfo)(statement: (String, Map[String, Any]) => Unit) extends DataSource with Pipeline {

  override val driverClass: String = "org.postgresql.Driver"

  override def process[Result](pageResultItem: (String, Result)): Unit = {
    val (_, resultMap) = (pageResultItem._1, pageResultItem._2.asInstanceOf[Map[String, Any]])
    statement(dataSouceInfo.database, resultMap)
  }

  override def init(): Unit = DataSource.initConnection(driverClass, dataSouceInfo)
}
