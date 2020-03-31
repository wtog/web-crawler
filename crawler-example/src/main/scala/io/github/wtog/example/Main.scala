package io.github.wtog.example

import io.github.wtog.crawler.processor.PageProcessor
import io.github.wtog.crawler.spider.Spider
import io.github.wtog.utils.ReflectionUtils

import scala.io.StdIn
import scala.util.Try

/**
  * @author : tong.wang
  * @since : 6/14/18 11:40 PM
  * @version : 1.0.0
  */
object Main {

  def printProcessors(processorList: Seq[(ExampleTrait, Int)]): Unit = {
    for ((service, order) ← processorList) {
      println(s"\t${order}. ${service.getClass.getSimpleName}")
    }
    println("")
  }

  def main(args: Array[String]): Unit = {
    val processorList = ReflectionUtils
      .implementationClasses(classOf[ExampleTrait], "io.github.wtog.example")
      .map(_.newInstance())
      .filter(_.enable)
      .sortWith(_.getClass.getSimpleName < _.getClass.getSimpleName)
      .zip(Stream.from(1))

    val execProcessors = args match {
      case args: Array[String] if args.isEmpty || args.contains("0") ⇒
        println("executing all enabled processors")
        printProcessors(processorList)
        processorList
      case args: Array[String] if (args.nonEmpty && args.toSeq.forall(arg ⇒ Try(arg.toInt).isSuccess)) ⇒
        val processors = processorList.filter {
          case (_, order) ⇒
            args.contains(order.toString)
        }
        println(s"executing ${processors.map(_._1.getClass.getSimpleName).mkString(",")}")
        processors
      case _ ⇒
        println("\nshow page processor list: ")
        println("\t0. all")

        printProcessors(processorList)

        println("\nchoose number to execute.")
        println("input like 1,2,3 means to execute 1 and 2 and 3 processor")

        val chooseNumber = StdIn.readLine()
        val chosen       = chooseNumber.split(",").distinct

        val executeProcessor =
          if (chosen.isEmpty || chooseNumber.contains("0")) {
            println("execute all processor")
            processorList
          } else {
            processorList.filter {
              case (_, order) ⇒ chosen.contains(order.toString)
            }
          }

        executeProcessor
    }

    startSpiders(execProcessors)
  }

  def startSpiders(processorList: Seq[(PageProcessor, Int)]): Unit =
    processorList.foreach {
      case (processor, _) ⇒
        Spider(pageProcessor = processor).start()
    }
}
