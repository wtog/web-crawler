package io.github.wtog.example

import java.util.concurrent.atomic.AtomicInteger

import io.github.wtog.crawler.pipeline.file.CsvFilePipeline
import io.github.wtog.crawler.processor.{ Page, PageProcessor, RequestSetting }
import io.github.wtog.utils.JsonUtils
import org.jsoup.nodes.Element

import scala.concurrent.duration._

/**
  * @author : tong.wang
  * @since : 10/11/19 10:01 APM
  * @version : 1.0.0
  */
class LianjiaErshouFangProcessor extends PageProcessor {
  override def targetUrls: List[String] = List("https://bj.lianjia.com/ershoufang/pg1/")

  val pageNo           = new AtomicInteger(1)
  val houseDetailRegex = """(.*ershoufang/[\d]+.html$)""".r
  val houseListRegex   = """(.*ershoufang/pg[\d]+/$)""".r

  val queryDomValue   = (typ: String, elements: Map[String, Seq[Element]], getDomValue: Element => String) => elements.get(typ).fold("")(e => getDomValue(e.head))
  val getLiText       = (e: Element) => e.childNodes.get(1).toString
  val getLastSpanText = (e: Element) => e.select("span").last().text()

  override def doProcess(page: Page) =
    page.requestSetting.url.get match {
      case houseListRegex(_) =>
        addHouseDetail(page)
        page.addTargetRequest(s"https://bj.lianjia.com/ershoufang/pg${pageNo.incrementAndGet()}/")
      case houseDetailRegex(detail) =>
        val overviewContent      = page.dom(".overview .content")
        val price                = overviewContent.select(".price")
        val priceTotal           = price.getText(".total")
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
        val communityName        = aroundInfo.getElements("a").first().text()
        val communityAreaName    = aroundInfo.getText(".areaInfo")

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
          url = detail,
          totalPrice = priceTotal,
          meterPrice = pricePerMeter,
          roomMainInfo = roomMainInfoText,
          roomSubInfo = roomSubInfoText,
          roomTypeMainInfo = roomTypeMainInfoText,
          roomTypeSubInfo = roomTypeSubInfoText,
          roomAreaMainInfo = roomAreaMainInfo,
          roomAreaSubInfo = roomAreaSubInfo,
          communityName = communityName,
          communityAreaName = communityAreaName,
          buildType = buildType,
          buildStruct = buildStruct,
          decoration = decoration,
          householdLadder = householdLadder,
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

        page.addPageResultItem(house.toMap)
      case other ⇒
        println(other)
    }

  def addHouseDetail(page: Page) = {
    val detailHrefs = page.dom(".sellListContent li div.title a")
    detailHrefs.toSeq.foreach(d => page.addTargetRequest(d.attr("href")))
  }

  override def pipelines = Set(CsvFilePipeline(fileName = Some("BeijingErshouFang")))

  override def requestSetting: RequestSetting = RequestSetting(domain = "bj.lianjia.com", sleepTime = 2 seconds)

}

case class House(
    houseType: String = HouseType.ERSHOU.toString,
    url: String, //
    totalPrice: String,
    meterPrice: String,
    roomMainInfo: String,
    roomSubInfo: String,
    roomTypeMainInfo: String,
    roomTypeSubInfo: String,
    roomAreaMainInfo: String,
    roomAreaSubInfo: String,
    communityName: String,
    communityAreaName: String,
    buildType: String,       //建筑类型
    buildStruct: String,     // 建筑结构
    decoration: String,      //装修情况
    householdLadder: String, //梯户比例
    heating: String,         //供暖
    elevator: String,        //电梯
    houseRight: String,      //产权
    saleTime: String,        //挂牌时间
    tradingRight: String,    //交易权属
    lastSale: String,        //上次交易
    housingUse: String,      //房屋用途
    houseYears: String,      //房屋年限
    houseRightOwner: String, //产权所属
    mortgageInfo: String     //抵押信息
  )

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
