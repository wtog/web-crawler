package io.github.wtog.selector

import org.json4s.native.Serialization
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import us.codecraft.xsoup.{ XElements, Xsoup }

/**
 * @author : tong.wang
 * @since : 5/18/18 12:32 AM
 * @version : 1.0.0
 */
case class HtmlParser(text: String, url: String) {

  private[HtmlParser] object JsoupParser {
    def document: Document = Jsoup.parse(text, url)
  }

  def document: Document = JsoupParser.document

  def json = HtmlParser.parseJson(text)

  def xpath(xpath: String): XElements = Xsoup.select(text, xpath)
}

object HtmlParser {
  import org.json4s.native.JsonMethods._
  import org.json4s._
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
