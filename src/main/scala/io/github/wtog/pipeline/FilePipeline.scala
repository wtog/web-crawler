package io.github.wtog.pipeline

import java.io.PrintWriter

/**
 * @author : tong.wang
 * @since : 2019-01-20 00:47
 * @version : 1.0.0
 */
case class FilePipeline(fileDir: String) extends Pipeline {

  override def process(pageResultItem: (String, Map[String, Any])): Unit = {
    val (_, result) = pageResultItem
    val fileName = result("fileName").asInstanceOf[String]
    val content = result("content")

    if (!fileName.isEmpty)
      new PrintWriter(s"${fileDir}/${fileName.replace("/", "-")}.html") {
        try {
          write(s"${content}")
        } finally {
          println("finally executed....")
          close()
        }
      }
  }
}
