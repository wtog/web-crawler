package io.github.wtog.utils

import com.typesafe.config.{ ConfigException, ConfigFactory }
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

/**
  * @author : tong.wang
  * @since : 2019-05-09 00:14
  * @version : 1.0.0
  */
object ConfigUtils {

  private[this] lazy val logger = LoggerFactory.getLogger(ConfigUtils.getClass)

  private[this] lazy val config = ConfigFactory.load().getConfig("web-crawler")

  def getSeq[T](path: String): Seq[T] = config.getList(path).unwrapped().asScala.map(_.asInstanceOf[T])

  def getStringOpt(path: String) = getOpt[String](path)(config.getString)

  def getIntOpt(path: String) = getOpt[Int](path)(config.getInt)

  private[this] def getOpt[T](path: String)(getConfig: String => T): Option[T] =
    Try(getConfig(path)) match {
      case Success(value) =>
        Some(value)
      case Failure(e: ConfigException.Missing) =>
        None
      case Failure(e) =>
        logger.error(s"failed to get config: ${path}", e.getLocalizedMessage)
        None
    }

}
