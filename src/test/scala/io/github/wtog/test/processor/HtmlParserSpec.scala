package io.github.wtog.test.processor

import io.github.wtog.processor.{Page, RequestSetting}
import io.github.wtog.selector.HtmlParser
import io.github.wtog.test.BaseTest

/**
  * @author : tong.wang
  * @since : 2019-05-02 21:49
  * @version : 1.0.0
  */
class HtmlParserSpec extends BaseTest with HtmlParser {

  test("page json") {
    val pageJsonObj =
      """
        |{
        |  "id": 1,
        |  "name": "test"
        |}
      """.stripMargin

    val pageJson = Page(bytes = Some(pageJsonObj.getBytes()), requestSetting = RequestSetting()).json().asInstanceOf[Map[String, Any]]

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

    val pageJsonList = Page(bytes = Some(pageListJson.getBytes()), requestSetting = RequestSetting()).json().asInstanceOf[List[Map[String, Any]]]

    assert(pageJsonList.head("id") == 1)
    assert(pageJsonList.head("name") == "test")

  }
}
