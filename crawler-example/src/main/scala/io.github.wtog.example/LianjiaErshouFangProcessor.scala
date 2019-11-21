package io.github.wtog.example

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

import io.github.wtog.crawler.pipeline.Pipeline
import io.github.wtog.crawler.pipeline.db.{ DataSource, DataSourceInfo, PostgreSQLPipeline }
import io.github.wtog.crawler.processor.{ Page, PageProcessor, RequestSetting }
import io.github.wtog.utils.JsonUtils
import io.github.wtog.utils.StringUtils._
import org.jsoup.nodes.Element

import scala.concurrent.duration._
import scala.util.Random

/**
  * @author : tong.wang
  * @since : 10/11/19 10:01 APM
  * @version : 1.0.0
  */
class LianjiaErshouFangProcessor extends PageProcessor {
  val pageNo           = new AtomicInteger(1)
  val houseDetailRegex = """(.*ershoufang)/([\d]+).(html$)""".r
  val houseListRegex   = """(.*ershoufang/pg[\d]+/$)""".r

  val queryDomValue   = (typ: String, elements: Map[String, Seq[Element]], getDomValue: Element => String) => elements.get(typ).fold("")(e => getDomValue(e.head))
  val getLiText       = (e: Element) => e.childNodes.get(1).toString
  val getLastSpanText = (e: Element) => e.select("span").last().text()

  def getPage = if (pageNo.get() > 100) pageNo.set(0) else pageNo.incrementAndGet()

  override def doProcess(page: Page): Unit =
    page.requestSetting.url.get match {
      case houseListRegex(_) =>
        addHouseDetail(page)
        page.addTargetRequest(s"https://bj.lianjia.com/ershoufang/pg${getPage}/")
      case houseDetailRegex(_, houseCode, _) =>
        val overviewContent = page.dom(".overview .content")
        val price           = overviewContent.select(".price")
        val pageShoufu      = page.dom(".new-calculator").attr("data-shoufu")

        val (evaluationPrice, priceTotal) = pageShoufu match {
          case shoufu if shoufu.nonEmpty =>
            val newCalculator = JsonUtils.parseFrom[Map[String, Any]](shoufu)
            val evaluation    = newCalculator.get("evaluation").get.asInstanceOf[Int]
            val total         = newCalculator.get("price").fold(0)(_.asInstanceOf[String].toInt)
            (evaluation, total)
          case "" =>
            (0, 0)
        }

        val pricePerMeter        = price.getText(".unitPriceValue").replace("元/平米", "")
        val room                 = overviewContent.getElements(".room")
        val roomMainInfoText     = room.getText(".mainInfo")
        val roomSubInfoText      = room.getText(".subInfo")
        val roomType             = overviewContent.getElements(".type")
        val roomTypeMainInfoText = roomType.getText(".mainInfo")
        val roomTypeSubInfoText  = roomType.getText(".subInfo")
        val roomArea             = overviewContent.getElements(".area")
        val roomAreaMainInfo     = roomArea.getText(".mainInfo").replace("平米", "")
        val roomAreaSubInfo      = roomArea.getText(".subInfo")
        val aroundInfo           = overviewContent.getElements(".aroundInfo")
        val subdistrict          = aroundInfo.getElements("a").first().text()
        val communityAreaName    = aroundInfo.getText(".areaName").replace("所在区域", "").split(" ")

        val (areaName, community, communityDetail) = (communityAreaName.headOption, communityAreaName.tail.headOption, communityAreaName.lastOption)

        val infoContent = page.dom(".m-content .base .content li")
        val basic       = infoContent.toSeq.groupBy(e => e.select("span").text)

        val buildType               = queryDomValue("建筑类型", basic, getLiText)
        val buildStruct             = queryDomValue("建筑结构", basic, getLiText)
        val decoration: String      = queryDomValue("装修情况", basic, getLiText)
        val householdLadder: String = queryDomValue("梯户比例", basic, getLiText)
        val heating: String         = queryDomValue("供暖方式", basic, getLiText)
        val elevator: String        = queryDomValue("配备电梯", basic, getLiText)
        val houseRight: String      = queryDomValue("产权年限", basic, getLiText)

        val transactionContent = page.dom(".m-content .transaction .content li")

        val info                    = transactionContent.toSeq.groupBy(e => e.select("span").first().text)
        val saleTime: String        = queryDomValue("挂牌时间", info, getLastSpanText)
        val tradingRight: String    = queryDomValue("交易权属", info, getLastSpanText)
        val lastSale: String        = queryDomValue("上次交易", info, getLastSpanText)
        val housingUse: String      = queryDomValue("房屋用途", info, getLastSpanText)
        val houseYears: String      = queryDomValue("房屋年限", info, getLastSpanText)
        val houseRightOwner: String = queryDomValue("产权所属", info, getLastSpanText)
        val mortgageInfo: String    = queryDomValue("抵押信息", info, getLastSpanText)

        val house = House(
          houseCode = houseCode,
          totalPrice = priceTotal.toInt,
          evaluationPrice = evaluationPrice,
          meterPrice = pricePerMeter.toInt,
          roomMainInfo = roomMainInfoText,
          roomSubInfo = roomSubInfoText,
          roomTypeMainInfo = roomTypeMainInfoText,
          roomTypeSubInfo = roomTypeSubInfoText,
          roomAreaMainInfo = roomAreaMainInfo,
          roomAreaSubInfo = roomAreaSubInfo,
          subdistrict = subdistrict,
          areaName = areaName,
          community = community,
          communityDetail = communityDetail,
          buildType = buildType,
          buildStruct = buildStruct,
          decoration = decoration,
          householdladder = householdLadder,
          heating = heating,
          elevator = elevator,
          houseRight = houseRight,
          saleTime = saleTime,
          tradingRight = tradingRight,
          lastSale = lastSale,
          housingUse = housingUse,
          houseYears = houseYears,
          houseRightOwner = houseRightOwner,
          mortgageInfo = mortgageInfo
        )

        page.addPageResultItem[Map[String, Any]](house.toMap)
      case other ⇒
        println(other)
    }

  def addHouseDetail(page: Page) = {
    val detailHrefs = page.dom(".sellListContent li div.title a")
    detailHrefs.toSeq.foreach(d => page.addTargetRequest(d.attr("href")))
  }

  override def pipelines: Set[Pipeline] = Set(
    //    CsvFilePipeline(Some("ershoufang.csv")),
    PostgreSQLPipeline(DataSourceInfo(database = this.getClass.getSimpleName, jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/magicbox", username = "wtog", password = "")) { (db: String, result: Map[String, Any]) =>
      val (keys, values) = result.unzip
      DataSource.rows[Int]("select count(1) from house where house_code = ?", Seq(result("houseCode").asInstanceOf[String]))(r => r.getInt(1))(db).headOption.getOrElse(0) match {
        case 0 =>
          DataSource.executeUpdate(s"insert into house (${keys.map(_.toUnderscore).mkString(",")}) values (${Seq.fill[String](keys.size)("?").mkString(",")})", values.toSeq)(db)
        case _ =>
          DataSource.executeUpdate(
            s"update house set ${keys.map(c => s"${c.toUnderscore} = ?").mkString(",")}, updated_at = '${ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}' where house_code = ? ",
            (values.toSeq ++ Seq(result("houseCode")))
          )(db)
      }
    }
  )

  override def requestSetting: RequestSetting = RequestSetting(
    domain = "www.lianjia.com",
    sleepTime = (Random.nextInt(3) + 5) seconds,
    useProxy = true
  )

  override def targetUrls: List[String] = List("https://bj.lianjia.com/ershoufang/pg1/")

}

case class House(
    id: Option[Int] = None,
    houseCode: String,
    totalPrice: Int,
    evaluationPrice: Int,
    meterPrice: Int,
    roomMainInfo: String,
    roomSubInfo: String,
    roomTypeMainInfo: String,
    roomTypeSubInfo: String,
    roomAreaMainInfo: String,
    roomAreaSubInfo: String,
    subdistrict: String,
    areaName: Option[String],
    community: Option[String],
    communityDetail: Option[String],
    buildType: String,
    buildStruct: String,
    decoration: String,
    householdladder: String,
    heating: String,
    elevator: String,
    houseRight: String,
    saleTime: String,
    tradingRight: String,
    lastSale: String,
    housingUse: String,
    houseYears: String,
    houseRightOwner: String,
    mortgageInfo: String)

object House {

  implicit class HouseWrapper(house: House) {
    def toMap: Map[String, Any] = JsonUtils.toMap(house)
  }

}

object HouseType extends Enumeration {
  type HouseType = Value

  val ERSHOU = Value("ershou")
  val NEW    = Value("new")
}
