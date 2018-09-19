package io.github.wtog

import com.google.common.reflect.{ ClassPath, TypeToken }
import io.github.wtog.processor.PageProcessor
import io.github.wtog.spider.Spider

import scala.io.StdIn

/**
 * @author : tong.wang
 * @since : 6/14/18 11:40 PM
 * @version : 1.0.0
 */
object Main {

  def main(args: Array[String]): Unit = {
    import scala.collection.JavaConverters._

    val classPath = ClassPath.from(this.getClass.getClassLoader)

    val classes = classPath.getTopLevelClassesRecursive("io.github.wtog.processor.impl").asScala
    val pageProcessor = classOf[PageProcessor]
    val processorList = classes.map(_.load()).filter { clazz ⇒
      val types = TypeToken.of(clazz).getTypes.asScala
      types.exists(_.getRawType == pageProcessor)
    }.map { clazz ⇒
      val constructor = clazz.getConstructors.head
      constructor.newInstance().asInstanceOf[PageProcessor]
    }.toList.zip(Stream from 1)

    System.getenv("PASS_PLATFORM") match {
      case "openshift" ⇒
        processorList.map(it ⇒ it._1).foreach(it ⇒ Spider(pageProcessor = it).start())
      case _ ⇒
        println("show page processor list: ")
        println("\t0. all")
        for ((service, order) ← processorList) {
          println(s"\t${order}. ${service.getClass.getSimpleName}")
        }

        println("\nchoose number to execute.")
        println("input like 1,2,3 means to execute 1 and 2 and 3 processor")

        val chooseNumber = StdIn.readLine()
        val chosen = chooseNumber.split(",").distinct

        val executeProcessor = if (chosen.isEmpty || chooseNumber.contains("0")) {
          println("execute all processor")
          processorList.map(it ⇒ it._1)
        } else {
          processorList.collect {
            case (processor, order) if chosen.contains(order.toString) ⇒ processor
          }
        }

        val spiders = executeProcessor.map(it ⇒ Spider(pageProcessor = it))

        spiders.foreach(_.start())
    }
  }
}
