package io.github.wtog.pipeline

import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

import io.github.wtog.utils.UrlUtils

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

/**
  * @author : tong.wang
  * @since : 5/20/18 11:01 PM
  * @version : 1.0.0
  */
object CsvFilePipeline extends Pipeline {

  override def process(pageResultItem: (String, Map[String, Any])): Unit = {
    val (pageUrl, resultItems) = pageResultItem
    IOContentCache.add(UrlUtils.getDomainAndURI(pageUrl), resultItems, 5)
  }
}

object IOContentCache {
  var cache: Map[String, Option[(Long, ListBuffer[Map[String, Any]])]] = Map()
  var fileIOCache: Map[String, RandomAccessFile] = Map()

  def add(key: String, value: Map[String, Any], expire: Long) = {
    if (cache.contains(key)) {
      val (_, l) = cache(key).get
      l.append(value)
      cache += (key -> Some((TimeUnit.MINUTES.toMillis(expire) + System.currentTimeMillis(), l)))
    } else {
      val l = new ListBuffer[Map[String, Any]]
      l.append(value)
      cache += (key -> Some((TimeUnit.MINUTES.toMillis(expire) + System.currentTimeMillis(), l)))
    }
  }

  def writeContentFile(fileName: String, contentList: ListBuffer[Map[String, Any]], closeFile: Boolean) = {
    val file = UrlUtils.getDomainAndURI(if (fileName.contains("/")) fileName.replace("/","_") else fileName)

    val randomFile = if (fileIOCache.contains(file)) fileIOCache(file)
                      else {
                        val rf = new RandomAccessFile(s"/tmp/web-crawler-${file}.csv", "rw")
                        fileIOCache += (file -> rf)
                        rf
                      }
    try {
       randomFile.length match {
         case fileLength if fileLength == 0 =>
           randomFile.seek(fileLength)//指针指向文件末尾
           val title = contentList.head.keys.mkString(",") + "\n"
           randomFile.write((title).getBytes("UTF-8"))
           val row = contentList.head.values.mkString(",") + "\n"
           randomFile.write((row).getBytes("UTF-8"))
         case fileLength if fileLength > 0 =>
           randomFile.seek(fileLength)//指针指向文件末尾
           contentList.foreach(map => {
             val row = map.values.mkString(",")  + "\n"
             randomFile.write((row).getBytes("UTF-8"))//写入数据
           })
       }
     } catch {
       case ex: Throwable => ex.printStackTrace()
     } finally {
       if (closeFile)
        randomFile.close()
    }
  }

  def get(key: String) = cache(key)

  val expire: Unit = {
    def removeExpire() = {
      while (true) {
        cache.foreach(c => {
          c._2.foreach(it => {
            val (expire, list) = it
            if (System.currentTimeMillis() > expire) {
              if (list.isEmpty) {
                cache -= (c._1)
              } else {
                writeContentFile(c._1, list, closeFile = true)
                list.clear()
              }
            } else {
              val listSize = list.size
              if (listSize > 10) {
                writeContentFile(c._1, list.slice(0, 10), closeFile = false)
                cache += (c._1 -> Some((expire, list.slice(10, listSize))))
              }
            }
          })
        })
        TimeUnit.SECONDS.sleep(1)
      }
    }

    import scala.concurrent.ExecutionContext.Implicits.global

    Future {
      removeExpire()
    }.recover {
      case ex =>
        println(ex.getLocalizedMessage)
        removeExpire()
    }

  }

}