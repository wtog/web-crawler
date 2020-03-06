package io.github.wtog.crawler.pipeline.db

import java.sql._
import java.util.concurrent.ConcurrentHashMap

import com.zaxxer.hikari.HikariDataSource
import io.github.wtog.utils.logger.Logging

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
        hikariDataSource.setValidationTimeout(1000)
        hikariDataSource.setPoolName(database)

        pools.put(database, hikariDataSource)
      }
    } catch {
      case NonFatal(e) =>
        throw e
    }

  def getConnection(db: String): Connection = pools.get(db).getConnection

  def executeQuery[R](sql: SQL)(wrapper: ResultSet => R)(implicit db: String): Seq[R] =
    buildStatement(sql) { statement =>
      val resultSet = statement.executeQuery()
      val results   = new ListBuffer[R]

      while (resultSet.next()) {
        results.append(wrapper(resultSet))
      }
      results.toSeq
    }

  def executeUpdate(sql: String, parameters: Seq[Any])(implicit db: String): Int =
    buildStatement(SQL(sql, parameters))(statement => statement.executeUpdate())

  private def buildStatement[R](sql: SQL)(exec: PreparedStatement => R)(implicit database: String): R = {
    val conn = DataSource.getConnection(database)
    try {
      val statement  = conn.prepareStatement(sql.sql)
      var index: Int = 1

      for (p <- sql.parameters) {
        p match {
          case p: Int =>
            statement.setInt(index, p)
          case p: String =>
            statement.setString(index, p)
          case p: Boolean =>
            statement.setBoolean(index, p)
          case other =>
            throw new UnsupportedOperationException(s"parameter ${other}:${other.getClass.getName} not support by now ")
        }
        index += 1
      }
      logger.debug(s"${sql}")
      exec(statement)
    } finally {
      conn.close()
    }

  }

  def rows[R](sql: String, parameters: Seq[Any])(wrapper: ResultSet => R)(implicit db: String): Seq[R] =
    executeQuery(SQL(sql, parameters))(wrapper)
}

case class SQL(sql: String, parameters: Seq[Any]) {

  import io.github.wtog.utils.StringUtils._

  override def toString: String = sql.placeholderReplacedBy("\\?", parameters: _*)
}
