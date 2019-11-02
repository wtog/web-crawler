package io.github.wtog.utils.test.jmh

import org.openjdk.jmh.annotations.Benchmark

import scala.collection.mutable.ListBuffer

/**
  * @author : tong.wang
  * @since : 11/10/19 11:25 AM
  * @version : 1.0.0
  */
class StringUtilsBenchmark {
  import StringUtilsBenchmark._

  @Benchmark
  def foldleft(): String = {
    foldLeft()
  }

  @Benchmark
  def stringBuild: String = {
    stringbuilder()
  }

  @Benchmark
  def mapMkString: String = {
    map()
  }

  @Benchmark
  def stringutils: String = {
    stringUtils()
  }

  @Benchmark
  def replace: String = {
    replaceFirstFold()
  }
}

object StringUtilsBenchmark {
  val sql =
    """
      |update house set
      |trading_right = ?,
      |house_right_owner = ?,
      |community_name = ?,
      |community_area_name = ?,
      |house_code = ?,
      |room_type_sub_info = ?,
      |meter_price = ?,
      |heating = ?,
      |total_price = ?,
      |sale_time = ?,
      |elevator = ?,
      |room_type_main_info = ?,
      |householdladder = ?,
      |room_main_info = ?,
      |housing_use = ?,
      |last_sale = ?,
      |house_right = ?,
      |room_area_sub_info = ?,
      |build_type = ?,
      |build_struct = ?,
      |evaluation_price = ?,
      |mortgage_info = ?,
      |room_sub_info = ?,
      |decoration = ?,
      |room_area_main_info = ?,
      |house_years = ?
      |where
      |house_code = ? "
      |""".stripMargin

  val params = Seq.tabulate[Any](28){a => if (a % 2 == 0) "test" else a}

  def foldLeft(): String = {
    val list = sql.split('?') zip params

    list.foldLeft("") {
      case (s, (sql, param)) =>
        s"$s$sql$param"
    }
  }

  def stringbuilder(): String = {
    val list = sql.split('?') zip params
    val buffer = new StringBuilder()
    list.foreach {
      case (s,p) =>
        buffer.append(s).append(p)
    }
    buffer.toString()
  }

  def map(): String = {
    val list = sql.split('?') zip params

    list.map {
      case (s, p) =>
        s"$s$p"
    }.mkString("")
  }

  def replaceFirstFold(): String = {
    val buffer = new ListBuffer[Any]
    params.foreach(p =>
      buffer.append(sql.replaceFirst("\\?", p.toString)))
    buffer.mkString("")
  }

  def stringUtils(): String = {
    import io.github.wtog.utils.StringUtils._

    sql.placeholderReplacedBy("\\?", params)
  }
}