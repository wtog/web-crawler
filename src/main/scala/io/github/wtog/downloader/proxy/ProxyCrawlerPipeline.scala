package io.github.wtog.downloader.proxy

import io.github.wtog.pipeline.Pipeline

/**
  * @author : tong.wang
  * @since : 6/2/18 11:57 PM
  * @version : 1.0.0
  */
object ProxyCrawlerPipeline extends Pipeline {

  implicit def MapToProxyDTO(map: Map[String, String]): ProxyDTO =
    ProxyDTO(
      map("host"),
      map.getOrElse("port", "80").toInt,
      map.get("username"),
      map.get("password")
    )

  override def process[R](pageResultItem: (String, R)): Unit = {
    val (url, result) = pageResultItem

    val resultMap = result.asInstanceOf[Map[String, String]]
    if (logger.isDebugEnabled) {
      logger.debug(s"${url} => ${resultMap.slice(0, 3)}")
    }
    ProxyProvider.proxyList.offer(resultMap)
  }
}
