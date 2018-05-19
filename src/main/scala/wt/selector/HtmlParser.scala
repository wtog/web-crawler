package wt.selector

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, DocumentType}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try
import scala.util.control.NonFatal

/**
  * @author : tong.wang
  * @since : 5/18/18 12:32 AM
  * @version : 1.0.0
  */
case class HtmlParser(text: String,
                      url: String) {

  private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)

  object JsoupParser {
    def document: Try[Document] = Try(Jsoup.parse(text, url)).recover {
      case NonFatal(e) =>
        logger.error(s"failed parse html ${e.getLocalizedMessage}")
        throw e
    }
  }

}