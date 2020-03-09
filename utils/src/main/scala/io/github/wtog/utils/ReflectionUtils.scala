package io.github.wtog.utils

import com.google.common.reflect.{ClassPath, TypeToken}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * @author : tong.wang
  * @since : 11/19/18 11:39 PM
  * @version : 1.0.0
  */
object ReflectionUtils {
  private[this] lazy val CLASS_PATH = ClassPath.from(this.getClass.getClassLoader)

  def implementationClasses[T](clazz: Class[T], packageName: String): Seq[Class[T]] =
    getClasses[T](packageName)
      .filter { c ⇒
        !c.isInterface && TypeToken
          .of(c)
          .getTypes
          .asScala
          .exists(t ⇒ t.getRawType == clazz)
      }
      .toSeq

  def getClasses[T](packageName: String): mutable.Set[Class[T]] = CLASS_PATH.getTopLevelClassesRecursive(packageName).asScala.map(_.load().asInstanceOf[Class[T]])

}
