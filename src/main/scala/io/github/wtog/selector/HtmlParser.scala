package io.github.wtog.selector

import io.github.wtog.processor.Page
import io.github.wtog.utils.JsonUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import scala.collection.mutable.ListBuffer

/**
  * @author : tong.wang
  * @since : 5/18/18 12:32 AM
  * @version : 1.0.0
  */
trait HtmlParser {

  implicit class PageWrapper(page: Page) {

    import io.github.wtog.selector.HtmlParser._

    val document = Jsoup.parse(page.source, page.requestSetting.url.getOrElse(""))

    val title = document.title()

    val body = document.body()

    implicit def elementsAsScala(elements: Elements): Seq[Element] = {
      val buffer = new ListBuffer[Element]

      val size = elements.size()
      for (i <- 0 until size) {
        buffer.append(elements.get(i))
      }

      buffer
    }

    def div(element: String): Elements = document.select(element)

    def dom(query: String): Elements = document.select(query)

    def table(query: String): Seq[Element] = document.select(s"table ${query}")

    def hrefs: Seq[String] = {
      val hrefs            = new ListBuffer[String]
      val elementsIterator = document.select("a").listIterator()
      while (elementsIterator.hasNext) {
        val element = elementsIterator.next()
        element.attr("href") match {
          case h if h.startsWith("http") ⇒
            hrefs.append(element.attr("href"))
          case _ ⇒
        }
      }

      hrefs
    }

    def json[T: Manifest](text: Option[String] = None) = parseJson[T](text.getOrElse(page.source))

  }

}

object HtmlParser {

  def parseJson(json: String, key: String): Option[Any] = JsonUtils.parseFrom[Map[String, Any]](json).get(key)

  def parseJson[T: Manifest](json: String) = JsonUtils.parseFrom[T](json)

}
