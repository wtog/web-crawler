package io.github.wtog.utils

import com.typesafe.config.{ Config, ConfigFactory, ConfigValue }

import scala.collection.JavaConverters._
import java._

/**
  * @author : tong.wang
  * @since : 2019-05-09 00:14
  * @version : 1.0.0
  */
object ConfigUtils {

  private[this] lazy val config = ConfigFactory.load()

  def getSeq[T](path: String): Seq[T] = config.getList(path).unwrapped().asScala.map(v => v.asInstanceOf[T]).toSeq

  def getSeqMap(path: String): Seq[Map[String, Any]] = getSeq[util.Map[String, Any]](path).map(i => i.asScala.toMap)

  def getStringOpt(path: String): Option[String] = getOpt[String](path)(config.getString)

  def getIntOpt(path: String): Option[Int] = getOpt[Int](path)(config.getInt)

  def getBooleanOpt(path: String): Option[Boolean] = getOpt[Boolean](path)(config.getBoolean)

  def getConfig(name: String): Config = config.getConfig(name)

  def getKeyAndValue(name: String): Map[String, Any] = getConfig(name).entrySet().asScala.foldLeft(Map.empty[String, Any]) { (map, entry) =>
    map + (entry.getKey -> entry.getValue.unwrapped())
  }

  private[this] def getOpt[T](path: String)(getConfig: String => T): Option[T] =
    if (config.hasPath(path)) {
      Some(getConfig(path))
    } else {
      None
    }
}
