package io.github.wtog.crawler.downloader.proxy.crawler

import io.github.wtog.crawler.downloader.proxy.ProxyCrawlerPipeline
import io.github.wtog.crawler.pipeline.Pipeline
import io.github.wtog.crawler.processor.PageProcessor

/**
  * @author : tong.wang
  * @since : 9/16/18 10:34 AM
  * @version : 1.0.0
  */
trait ProxyProcessorTrait extends PageProcessor {
  override def cronExpression: Option[String] = Some("*/5 * * ? * *")

  override val pipelines: Set[Pipeline] = Set(ProxyCrawlerPipeline)
}
