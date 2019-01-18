package io.github.wtog.selector

import io.github.wtog.processor.Page
import io.github.wtog.selector.HtmlParser.toJson
import org.json4s.native.Serialization
import org.jsoup.Jsoup
import org.jsoup.select.Elements

import scala.collection.mutable.ListBuffer

/**
 * @author : tong.wang
 * @since : 5/18/18 12:32 AM
 * @version : 1.0.0
 */
trait HtmlParser {

  implicit class PageWrapper(page: Page) {
    val document = Jsoup.parse(page.source, page.requestGeneral.url.getOrElse(""))

    val title = document.title()
    val body = document.body()

    def div(element: String): Elements = {
      document.select(element)
    }

    def dom(query: String): Elements = {
      document.select(query)
    }

    def hrefs: Seq[String] = {
      val hrefs = new ListBuffer[String]
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

    def json = toJson(page.source)
  }
}

object HtmlParser {
  import org.json4s._
  import org.json4s.native.JsonMethods._
  implicit val serialize = Serialization.formats(NoTypeHints)

  implicit val formats = DefaultFormats

  def parseJson(json: String, key: String) = {
    parse(json) \\ key match {
      case JInt(num) ⇒ num.intValue()
      case other     ⇒ other
    }
  }

  def parseJson(json: String): Map[String, Any] = {
    parse(json).extract[Map[String, Any]]
  }

  def toJson(obj: Any): Map[String, Any] = {
    Extraction.decompose(obj).extract[Map[String, Any]]
  }
}
