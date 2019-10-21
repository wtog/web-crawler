package io.github.wtog.utils

import com.google.common.reflect.{ ClassPath, TypeToken }

import scala.collection.JavaConverters._

/**
  * @author : tong.wang
  * @since : 11/19/18 11:39 PM
  * @version : 1.0.0
  */
object ClassUtils {

  def loadClasses[T](clazz: Class[T], packageNames: String*): Seq[T] = {
    val classPath = ClassPath.from(this.getClass.getClassLoader)

    packageNames.flatMap { packageName ⇒
      val classes = classPath.getTopLevelClassesRecursive(packageName).asScala
      classes
        .map(_.load())
        .filter { c ⇒
          !c.isInterface && TypeToken
            .of(c)
            .getTypes
            .asScala
            .exists(t ⇒ t.getRawType == clazz)
        }
        .map { clazz ⇒
          val constructor = clazz.getConstructors.head
          constructor.newInstance().asInstanceOf[T]
        }
    }

  }
}
