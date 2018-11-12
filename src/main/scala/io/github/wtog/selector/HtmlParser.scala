package io.github.wtog.selector

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

  def json = {
    import org.json4s._
    import org.json4s.native.JsonMethods._
    implicit val formats = DefaultFormats

    parse(text).extract[Map[String, Any]]
  }

  def xpath(xpath: String): XElements = Xsoup.select(text, xpath)
}
