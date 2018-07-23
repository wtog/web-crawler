package io.wt.utils

/**
  * @author : tong.wang
  * @since : 5/19/18 5:20 PM
  * @version : 1.0.0
  */
object UrlUtils {
  val domainRegex = """[\w]+://""".r
  val chartsetRegex = """charset\s*=\s*['"]*([^\s;'"]*)""".r

  def getDomain(url: String): String = {
    val urlPath = domainRegex.replaceFirstIn(url, "")

    if (urlPath.indexOf("/") > 0) {
      urlPath.substring(0, urlPath.indexOf("/", 1)).replaceAll(""":[\d]{1,}""", "")
    } else {
      urlPath
    }
  }

  def fixIllegalCharacterInUrl(url: String): String = {
    url.replace(" ", "%20").replaceAll("#+", "#")
  }

  def getCharset(contentType: String): Option[String] = {
    chartsetRegex.findFirstIn(contentType)
  }

  def getDomainAndURI(url:String): String = {
    domainRegex.replaceFirstIn(url, "").substring(0, url.indexOf("?"))
  }

  def removePort(url: String) = {
    val domain = getDomain(url)
    val portIndex = domain.indexOf(":")
    if (portIndex == -1) {
      domain
    } else {
      domain.substring(0, portIndex)
    }
  }
}
