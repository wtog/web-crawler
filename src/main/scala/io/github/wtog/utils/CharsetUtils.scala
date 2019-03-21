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

  def getHtmlSourceWithCharset(contentBytes: Array[Byte], defaultCharset: String = Charset.defaultCharset().name()): String = {

    val content         = new String(contentBytes, defaultCharset)
    val metas: Elements = Jsoup.parse(content).select("meta")

    val metaContent = metas.attr("content")

    val actualCharset = if (metaContent.contains("charset")) { // html4
      metaContent
        .substring(metaContent.indexOf("charset"), metaContent.length)
        .split("=")(1)
    } else { // html5
      metas.attr("charset")
    }

    if (actualCharset.isEmpty || actualCharset.toUpperCase.equals(
          defaultCharset.toString.toUpperCase
        )) {
      content
    } else {
      new String(contentBytes, actualCharset)
    }
  }

}
