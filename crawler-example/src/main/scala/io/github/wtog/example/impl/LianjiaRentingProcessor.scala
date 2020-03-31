package io.github.wtog.example.impl

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

import io.github.wtog.crawler.dto.{ Page, RequestSetting }
import io.github.wtog.crawler.pipeline.Pipeline
import io.github.wtog.crawler.pipeline.db.{ DataSource, DataSourceInfo, PostgreSQLPipeline }
import io.github.wtog.example.ExampleTrait
import io.github.wtog.utils.JsonUtils
import io.github.wtog.utils.StringUtils._
import org.jsoup.nodes.Element

import scala.concurrent.duration._
import scala.util.Random
import scala.util.matching.Regex

/**
  * @author : tong.wang
  * @since : 10/11/19 10:01 APM
  * @version : 1.0.0
  */
class LianjiaRentingProcessor extends ExampleTrait {
  val pageNo                  = new AtomicInteger(1)
  val houseDetailRegex: Regex = """(.*zufang)/([BJ\d]+).(html$)""".r
  val houseListRegex: Regex   = """(.*)/(zufang)/(pg[\d]+/$)""".r
  val houseCodeRegex: Regex   = """[BJ\d]+""".r
  val houseFloor: Regex       = """[\d]+""".r

  val queryDomValue: (String, Map[String, Seq[Element]], Element => String) => String = (typ: String, elements: Map[String, Seq[Element]], getDomValue: Element => String) => elements.get(typ).fold("")(e => getDomValue(e.head))
  val getLiText: Element => String                                                    = (e: Element) => e.childNodes.get(1).toString
  val getLastSpanText: Element => String                                              = (e: Element) => e.select("span").last().text()

  def getPage: Int = if (pageNo.get() >= 100) pageNo.getAndSet(1) else pageNo.incrementAndGet()

  override def doProcess(page: Page): Unit =
    page.requestSetting.url.get match {
      case houseListRegex(domain, _, _) =>
        val details = page.dom(".content__list--item").toSeq

        details.foreach { detail =>
          val rentingWay = detail.select(".content__list--item--title").text().substring(0, 2)
          val href       = s"${domain}${detail.select("a").attr("href")}"

          val des = detail.select(".content__list--item--des").text().split("/")

          val area            = des.head.split("-")
          val areaName        = area.head.trim
          val community       = area.lift(1).get.trim
          val communityDetail = area.last.trim
          val meter           = des(1).replace("㎡", "").trim.toInt
          val direction       = des(2).trim
          val typ             = des(3).trim
          val floor           = des(4).trim

          val bottom = detail.select(".content__list--item--bottom i")

          val subway     = bottom.select(".content__item__tag--is_subway_house").text().trim
          val decoration = bottom.select(".content__item__tag--decoration").text().trim
          val heating    = bottom.select(".content__item__tag--central_heating").text().trim

          val rentingHouse = RentingHouse(
            houseCode = houseCodeRegex.findFirstIn(href).get,
            price = Some(detail.select(".content__list--item-price em").text().toInt),
            rentingWay = Some(rentingWay),
            area = Some(areaName),
            community = Some(community),
            communityDetail = Some(communityDetail),
            meter = Some(meter),
            direction = Some(direction),
            typ = Some(typ),
            floor = houseFloor.findFirstIn(floor).map(_.toInt),
            subway = if (subway.nonEmpty) Some(subway) else None,
            decoration = if (decoration.nonEmpty) Some(decoration) else None,
            heating = if (heating.nonEmpty) Some(heating) else None
          )
          page.addPageResultItem(JsonUtils.toMap(rentingHouse))
//          page.addTargetRequest(s"${domain}${detail.select("a").attr("href")}")
        }
        page.addTargetRequest(s"https://bj.lianjia.com/zufang/pg${getPage}/")
      case houseDetailRegex(_, houseCode, _) =>
      //todo append detail

      case other ⇒
        println(other)
    }

  override val pipelines: Set[Pipeline] = Set(
    PostgreSQLPipeline(DataSourceInfo(database = "renting", jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/magicbox", username = "wtog", password = "")) { (db: String, result: Map[String, Any]) =>
      val (keys, values) = result.unzip
      DataSource.rows[Int]("select count(1) from renting where house_code = ?", Seq(result("houseCode").asInstanceOf[String]))(r => r.getInt(1))(db).headOption.getOrElse(0) match {
        case 0 =>
          DataSource.executeUpdate(s"insert into renting (${keys.map(_.toUnderscore).mkString(",")}) values (${Seq.fill[String](keys.size)("?").mkString(",")})", values.toSeq)(db)
        case _ =>
          DataSource.executeUpdate(
            s"update renting set ${keys.map(c => s"${c.toUnderscore} = ?").mkString(",")}, updated_at = '${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}' where house_code = ? ",
            (values.toSeq ++ Seq(result("houseCode")))
          )(db)
      }
    }
  )

  override def requestSetting: RequestSetting = RequestSetting(
    domain = "bj.lianjia.com",
    sleepTime = (Random.nextInt(3) + 5) seconds,
    useProxy = true
  )

  override def targetUrls: List[String] = List("https://bj.lianjia.com/zufang/pg1/")

  override def cronExpression: Option[String] = Some("0 0/30 * * * ?")
}

case class RentingHouse(
    id: Option[Int] = None,
    houseCode: String,
    price: Option[Int] = None,
    rentingWay: Option[String] = None,
    area: Option[String] = None,
    community: Option[String] = None,
    communityDetail: Option[String] = None,
    meter: Option[Int] = None,
    direction: Option[String] = None,
    typ: Option[String] = None,
    floor: Option[Int] = None,
    subway: Option[String] = None,
    decoration: Option[String] = None,
    heating: Option[String] = None)
