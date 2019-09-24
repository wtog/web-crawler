package io.github.wtog.utils

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

/**
  * @author : tong.wang
  * @since : 9/24/19 11:05 PM
  * @version : 1.0.0
  */
object JsonUtils {

  private lazy val mapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.setSerializationInclusion(Include.NON_NULL)
    mapper.setSerializationInclusion(Include.NON_ABSENT)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  def toJson[T](t: T) = mapper.writeValueAsString(t)

  def toMap[T](t: T) = mapper.convertValue(t, classOf[Map[String, Any]])

  def parseFrom[T: Manifest](json: String) = mapper.readValue[T](json)

  case class Test(a: Int)
  def main(args: Array[String]): Unit = {
    val t = Test(1)
    val j = toJson(t)
    println(j)

    println(parseFrom[Test](j))
  }
}
