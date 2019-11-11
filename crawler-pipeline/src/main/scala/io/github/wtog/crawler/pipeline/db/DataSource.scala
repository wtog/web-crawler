package io.github.wtog.crawler.pipeline.db

import java.sql._
import java.util.concurrent.ConcurrentHashMap

import com.zaxxer.hikari.HikariDataSource
import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

/**
  * @author : tong.wang
  * @since : 10/29/19 8:27 PM
  * @version : 1.0.0
  */
trait DataSource extends Logging {

  protected val driverClass: String

}

object DataSource extends Logging {
  private val pools: ConcurrentHashMap[String, HikariDataSource] = new ConcurrentHashMap[String, HikariDataSource]()

  def initConnection(driverClass: String, dataSouceInfo: DataSourceInfo): Unit =
    try {
      val database = dataSouceInfo.database
      Option(pools.get(database)).getOrElse {
        val hikariDataSource = new HikariDataSource()
        hikariDataSource.setDriverClassName(driverClass)
        hikariDataSource.setJdbcUrl(dataSouceInfo.jdbcUrl)
        hikariDataSource.setUsername(dataSouceInfo.username)
        hikariDataSource.setPassword(dataSouceInfo.password)
        hikariDataSource.setMaximumPoolSize(dataSouceInfo.maxPoolSize)
        hikariDataSource.setMinimumIdle(dataSouceInfo.minIdleSize)
        hikariDataSource.setAutoCommit(true)
        hikariDataSource.setIdleTimeout(dataSouceInfo.idleTimeout.toMillis)
        hikariDataSource.setPoolName(database)

        pools.put(database, hikariDataSource)
      }
    } catch {
      case NonFatal(e) =>
        throw e
    }

  def getConnection(db: String): Connection = pools.get(db).getConnection

  def wrapper[V: Manifest](value: V) =
    value match {
      case v: String => s"'${v}'"
      case v         => s"${v}"
    }

  private def executeQuery[R](sql: SQL)(implicit db: String): ResultSet = {
    val statement = createStatement(sql)
    statement.executeQuery()
  }

  def executeUpdate(sql: String, parameters: Seq[Any])(implicit db: String): Int = {
    val statement = createStatement(SQL(sql, parameters))
    statement.executeUpdate()
  }

  private def createStatement(sql: SQL)(implicit database: String): PreparedStatement = {
    val conn       = DataSource.getConnection(database)
    val statement  = conn.prepareStatement(sql.sql)
    var index: Int = 1

    for (p <- sql.parameters) {
      p match {
        case p: Int =>
          statement.setInt(index, p)
        case (p: String) =>
          statement.setString(index, p)
        case other =>
          throw new UnsupportedOperationException(s"${other}")
      }
      index += 1
    }
    logger.debug(sql)
    statement
  }

  def rows[R](sql: String, parameters: Seq[Any])(wrapper: ResultSet => R)(implicit db: String): Seq[R] = {
    val resultSet = executeQuery(SQL(sql, parameters))
    val results   = new ListBuffer[R]
    while (resultSet.next()) {
      results.append(wrapper(resultSet))
    }
    results
  }
}

case class SQL(sql: String, parameters: Seq[Any]) {
  import io.github.wtog.utils.StringUtils._
  override def toString: String = sql.placeholderReplacedBy("\\?", parameters: _*)
}
