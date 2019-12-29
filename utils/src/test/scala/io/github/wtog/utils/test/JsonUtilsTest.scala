package io.github.wtog.utils.test

import io.github.wtog.utils.JsonUtils

/**
  * @author : tong.wang
  * @since : 12/29/19 8:25 PM
  * @version : 1.0.0
  */
class JsonUtilsTest extends BaseTest {

  test("json serialize and deserialize") {
    val test = Test("test")
    val json = JsonUtils.toJson(test)
    val map = JsonUtils.toMap(test)
    val jsonParsed = JsonUtils.parseFrom[Test](json)
    assert(test == jsonParsed)
    val mapParsed = JsonUtils.parseFrom[Test](map)
    assert(test == mapParsed)
  }
}

case class Test(t: String)
