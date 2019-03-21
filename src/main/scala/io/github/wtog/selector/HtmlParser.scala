package io.github.wtog.selector

import io.github.wtog.processor.Page
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

    import io.github.wtog.selector.HtmlParser._

    val document = Jsoup.parse(page.source, page.requestSetting.url.getOrElse(""))

    val title = document.title()
    val body  = document.body()

    def div(element: String): Elements = document.select(element)

    def dom(query: String): Elements = document.select(query)

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

    def json(text: Option[String] = None) = parseJson(text.getOrElse(page.source))

  }

}

object HtmlParser {

  import org.json4s._
  import org.json4s.native.JsonMethods._

  implicit val serialize = Serialization.formats(NoTypeHints)
  implicit val formats   = DefaultFormats

  def parseJson(json: String, key: String) = parse(json) \\ key match {
    case JInt(num) ⇒ num.intValue()
    case other     ⇒ other
  }

  def parseJson(json: String) = parse(json) match {
    case result: JArray =>
      result.extract[List[Map[String, Any]]]
    case result: JValue =>
      result.extract[Map[String, Any]]
    case other =>
      throw new IllegalArgumentException(s"unknown json type ${other}")
  }

  def toMap(obj: Any) = Extraction.decompose(obj).extract[Map[String, Any]]

}
