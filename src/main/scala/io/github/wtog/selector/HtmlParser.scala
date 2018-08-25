package io.github.wtog.selector

import org.jsoup.Jsoup
import org.jsoup.nodes.{ Document, DocumentType }
import org.slf4j.{ Logger, LoggerFactory }
import us.codecraft.xsoup.{ XElements, Xsoup }

import scala.util.Try
import scala.util.control.NonFatal

/**
 * @author : tong.wang
 * @since : 5/18/18 12:32 AM
 * @version : 1.0.0
 */
case class HtmlParser(text: Option[String], url: String) {

  private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(text: Option[String], url: String): HtmlParser = {
    text match {
      case text @ Some(_) ⇒ HtmlParser(text, url)
      case None           ⇒ throw new IllegalArgumentException("htmlParser text cant be none")
    }
  }

  private[HtmlParser] object JsoupParser {
    def document: Try[Document] = Try(Jsoup.parse(text.get, url)).recoverWith {
      case NonFatal(e) ⇒
        logger.error(s"failed parse html ${e.getLocalizedMessage}")
        throw e
    }
  }

  def document: Document = JsoupParser.document.get

  def getContentByXpath(xpath: String): XElements = {
    Xsoup.select(text.get, xpath)
  }
}
