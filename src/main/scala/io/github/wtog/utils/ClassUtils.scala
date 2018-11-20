package io.github.wtog.utils

import com.google.common.reflect.{ ClassPath, TypeToken }

import scala.collection.mutable

/**
 * @author : tong.wang
 * @since : 11/19/18 11:39 PM
 * @version : 1.0.0
 */
object ClassUtils {

  def loadClasses[T](packageName: String, clazz: Class[T]): mutable.Set[T] = {
    import collection.JavaConverters._
    val classPath = ClassPath.from(this.getClass.getClassLoader)

    val classes = classPath.getTopLevelClassesRecursive(packageName).asScala
    classes.map(_.load()).filter { c ⇒
      !c.isInterface && TypeToken.of(c).getTypes.asScala.exists(t ⇒ t.getRawType == clazz)
    }.map { clazz ⇒
      val constructor = clazz.getConstructors().head
      constructor.newInstance().asInstanceOf[T]
    }
  }
}
