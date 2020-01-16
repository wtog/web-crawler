package io.github.wtog.example.impl

import io.github.wtog.crawler.dto.{ Page, RequestSetting }
import io.github.wtog.example.ExampleTrait

import scala.concurrent.duration._

case class ZhihuAnswerPageProcessor() extends ExampleTrait {

  override def doProcess(page: Page): Unit = {
    val result = page.json[Map[String, Any]]()

    result.get("data").foreach { answers =>
      answers.asInstanceOf[List[Map[String, Any]]].foreach { answer =>
        val question      = answer("question").asInstanceOf[Map[String, String]]("title")
        val answerContent = answer("content")
        page.addPageResultItem(Map("question" -> question, "answer" -> answerContent))
      }
    }

    val nextPage = result("paging").asInstanceOf[Map[String, String]].get("next")

    nextPage.foreach { url =>
      page.addTargetRequest(url.replaceAll("https://www.zhihu.com", "$0/api/v4"))
    }
  }

  override def requestSetting: RequestSetting =
    RequestSetting(
      domain = "www.zhihu.com",
      timeOut = 10 seconds,
      sleepTime = 3 seconds
    )

  override def targetUrls: List[String] = List(
    "https://www.zhihu.com/api/v4/members/rednaxelafx/answers?include=data%5B*%5D.is_normal%2Cadmin_closed_comment%2Creward_info%2Cis_collapsed%2Cannotation_action%2Cannotation_detail%2Ccollapse_reason%2Ccollapsed_by%2Csuggest_edit%2Ccomment_count%2Ccan_comment%2Ccontent%2Cvoteup_count%2Creshipment_settings%2Ccomment_permission%2Cmark_infos%2Ccreated_time%2Cupdated_time%2Creview_info%2Cquestion%2Cexcerpt%2Cis_labeled%2Clabel_info%2Crelationship.is_authorized%2Cvoting%2Cis_author%2Cis_thanked%2Cis_nothelp%2Cis_recognized%3Bdata%5B*%5D.author.badge%5B%3F(type%3Dbest_answerer)%5D.topics&offset=0&limit=10&sort_by=created"
  )

  override def cronExpression: Option[String] = None
}
