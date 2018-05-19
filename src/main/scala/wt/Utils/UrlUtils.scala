package wt.Utils

/**
  * @author : tong.wang
  * @since : 5/19/18 5:20 PM
  * @version : 1.0.0
  */
object UrlUtils {
  val domainRegex = """[\w]+://""".r

  def getDomain(url: String): String = {
    val urlPath = domainRegex.replaceFirstIn(url, "")

    if (urlPath.indexOf("/") > 0) {
      urlPath.substring(0, urlPath.indexOf("/", 1)).replaceAll(""":[\d]{1,}""", "")
    } else {
      urlPath
    }
  }

}
