package io.github.wtog.crawler.downloader.proxy

import io.github.wtog.crawler.pipeline.Pipeline

/**
  * @author : tong.wang
  * @since : 6/2/18 11:57 PM
  * @version : 1.0.0
  */
object ProxyCrawlerPipeline extends Pipeline {

  override def process[R](pageResultItem: (String, R)): Unit = {
    val (url, result) = pageResultItem

    val resultMap = result.asInstanceOf[ProxyDTO]
    logger.trace(s"${url} => ${resultMap}")
    ProxyProvider.proxyList.offer(resultMap)
  }

}
