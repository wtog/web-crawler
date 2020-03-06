package io.github.wtog.crawler.selector

import java.nio.charset.Charset

import io.github.wtog.crawler.dto.Page
import io.github.wtog.utils.JsonUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import scala.collection.JavaConverters._
import org.jsoup.nodes.Document

/**
  * @author : tong.wang
  * @since : 5/18/18 12:32 AM
  * @version : 1.0.0
  */
trait HtmlParser {

  implicit class PageWrapper(page: Page) {

    lazy val document: Document = Jsoup.parse(page.source, page.requestSetting.url.getOrElse(""))

    lazy val title: String = document.title()

    lazy val body: Element = document.body()

    def div(element: String): Elements = document.select(element)

    def dom(query: String): Elements = document.select(query)

    def table(query: String): Seq[Element] = document.select(s"table ${query}").asScala.toSeq

    def hrefs: Seq[String] = document.select("a").toSeq.collect {
      case e if e.attr("href").startsWith("http") =>
        e.attr("href")
    }

  }

  implicit class ElementsWrapper(elements: Elements) {
    def getText(query: String): String = elements.select(query).text()

    def getElements(query: String): Elements = elements.select(query)

    def toSeq: Seq[Element] = elements.asScala.toSeq
  }

  implicit class ElementWrapper(element: Element) {
    def getText(query: String): String = element.select(query).text()
  }

}

object HtmlParser {

  def getValueFromJson[T: Manifest](json: String, key: String): Option[T] = JsonUtils.parseFrom[Map[String, T]](json).get(key)

  def parseJson[T: Manifest](json: String): T = JsonUtils.parseFrom[T](json)

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
