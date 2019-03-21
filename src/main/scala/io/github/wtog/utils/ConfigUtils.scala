package io.github.wtog.utils

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

/**
  * @author : tong.wang
  * @since : 2019-05-09 00:14
  * @version : 1.0.0
  */
object ConfigUtils {
  lazy val config = ConfigFactory.load().getConfig("web-crawler")

  def getSeq[T](path: String): Seq[T] = config.getList(path).unwrapped().asScala.map(_.asInstanceOf[T])
}
