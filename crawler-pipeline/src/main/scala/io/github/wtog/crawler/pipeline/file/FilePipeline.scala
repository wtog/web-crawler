package io.github.wtog.crawler.pipeline.file

import java.io.{ File, PrintWriter }

import io.github.wtog.crawler.pipeline.Pipeline

/**
  * @author : tong.wang
  * @since : 2019-01-20 00:47
  * @version : 1.0.0
  */
case class FilePipeline(fileBaseDir: Option[String] = None) extends Pipeline {

  override def process[R](pageResultItem: (String, R)): Unit = {
    val fileDto = pageResultItem._2.asInstanceOf[FileDTO]

    val (fileDirPath, fileName) = fileDto.fileName.lastIndexOf('/') match {
      case -1 =>
        (filePathFormat(fileBaseDir.getOrElse("/")), if (fileDto.fileName.startsWith("/")) fileDto.fileName else s"/${fileDto.fileName}")
      case fileDirIndex =>
        (filePathFormat(fileBaseDir.getOrElse("/")) + filePathFormat(fileDto.fileName.substring(0, fileDirIndex)), fileDto.fileName.substring(fileDirIndex))
    }

    val fileDir = new File(fileDirPath)

    !fileDir.exists() && fileDir.mkdirs()

    new PrintWriter(s"${fileDirPath}${fileName}.${fileDto.fileType}") {
      try {
        write(s"${fileDto.content}")
      } finally {
        close()
      }
    }
  }

  def filePathFormat(path: String): String = if (path.startsWith("/")) path else s"/${path}"
}

case class FileDTO(fileName: String, fileType: String = "html", content: String)
