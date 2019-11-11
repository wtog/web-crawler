package io.github.wtog.crawler.test.processor

import io.github.wtog.crawler.processor.{Page, RequestSetting}
import io.github.wtog.crawler.selector.HtmlParser
import io.github.wtog.crawler.test.BaseCoreTest

/**
  * @author : tong.wang
  * @since : 2019-05-02 21:49
  * @version : 1.0.0
  */
class HtmlParserSpec extends BaseCoreTest with HtmlParser {

  test("page json") {
    val pageJsonObj =
      """
        |{
        |  "id": 1,
        |  "name": "test"
        |}
      """.stripMargin

    val pageJson = Page(bytes = Some(pageJsonObj.getBytes()), requestSetting = RequestSetting()).json[Map[String, Any]]()

    assert(pageJson("id") == 1)
    assert(pageJson("name") == "test")

    val pageListJson =
      """
        |[
        |  {
        |    "id": 1,
        |    "name": "test"
        |  }
        |]
      """.stripMargin

    val pageJsonList = Page(bytes = Some(pageListJson.getBytes()), requestSetting = RequestSetting()).json[List[Map[String, Any]]]()

    assert(pageJsonList.head("id") == 1)
    assert(pageJsonList.head("name") == "test")

  }
}
