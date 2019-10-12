package io.github.wtog.selector

import io.github.wtog.processor.Page
import io.github.wtog.utils.JsonUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import scala.collection.JavaConverters._

/**
  * @author : tong.wang
  * @since : 5/18/18 12:32 AM
  * @version : 1.0.0
  */
trait HtmlParser {

  implicit class PageWrapper(page: Page) {

    val document = Jsoup.parse(page.source, page.requestSetting.url.getOrElse(""))

    val title = document.title()

    val body = document.body()

    def div(element: String): Elements = document.select(element)

    def dom(query: String): Elements = document.select(query)

    def table(query: String): Seq[Element] = document.select(s"table ${query}").asScala

    def hrefs: Seq[String] = document.select("a").toSeq.collect {
      case e if e.attr("href").startsWith("http") =>
        e.attr("href")
    }

  }

  implicit class ElementsWrapper(elements: Elements) {
    def getText(query: String): String = elements.select(query).text()

    def getElements(query: String): Elements = elements.select(query)

    def toSeq: Seq[Element] = elements.asScala
  }

  implicit class ElementWrapper(element: Element) {
    def getText(query: String): String = element.select(query).text()
  }

}

object HtmlParser {

  def getValueFromJson[T: Manifest](json: String, key: String): Option[T] = JsonUtils.parseFrom[Map[String, T]](json).get(key)

  def parseJson[T: Manifest](json: String) = JsonUtils.parseFrom[T](json)

}
