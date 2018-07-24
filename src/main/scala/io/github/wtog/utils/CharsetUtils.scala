package io.github.wtog.utils

import java.nio.charset.Charset

import org.jsoup.Jsoup
import org.jsoup.select.Elements

/**
  * @author : tong.wang
  * @since : 5/20/18 12:45 PM
  * @version : 1.0.0
  */
object CharsetUtils {
  def detectCharset(contentType: Option[String], contentBytes: Array[Byte]): (String, Option[String]) = {
    val charset = UrlUtils.getCharset(contentType.getOrElse(""))

    charset match {
      case Some(c) => (c, None)
      case None =>
        val defaultCharset = Charset.defaultCharset()
        val content = new String(contentBytes, defaultCharset)
        val metas: Elements = Jsoup.parse(content).select("meta")

        val metaContent = metas.attr("content")

        val actualCharset = if (metaContent.contains("charset")) { // html4
          metaContent.substring(metaContent.indexOf("charset"), metaContent.length).split("=")(1)
        } else { // html5
          metas.attr("charset")
        }

        if (actualCharset.isEmpty || actualCharset.toUpperCase.equals(defaultCharset.toString.toUpperCase)) {
          (defaultCharset.toString, Some(content))
        } else {
          (actualCharset, None)
        }
    }
  }

}
