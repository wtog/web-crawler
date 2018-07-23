package io.wt.downloader.proxy

import io.wt.pipeline.Pipeline

/**
  * @author : tong.wang
  * @since : 6/2/18 11:57 PM
  * @version : 1.0.0
  */
object ProxyCrawlerPipeline extends Pipeline {

  implicit def MapToProxyDTO(map: Map[String, Any]): ProxyDTO = {
    ProxyDTO(map("host").asInstanceOf[String],
             Integer.valueOf(map.getOrElse("port", "80").toString),
             Some(map.getOrElse("username", "").asInstanceOf[String]),
             Some(map.getOrElse("password", "").asInstanceOf[String]))
  }

  override def process(pageResultItem: (String, Map[String, Any])): Unit = {
    val (_, result) = pageResultItem

    ProxyProvider.proxyList += result
  }
}
