# web-crawler

[![Build Status](https://travis-ci.com/wtog/web-crawler.svg?branch=master)](https://travis-ci.com/wtog/web-crawler.svg?branch=master) [![codecov](https://codecov.io/gh/wtog/web-crawler/branch/master/graph/badge.svg)](https://codecov.io/gh/wtog/web-crawler) ![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.github.wtog/web-crawler_2.12.svg)

## 项目介绍

参考 webmagic [http://webmagic.io](http://webmagic.io) 撸的 [scala + akka] 爬虫

## 使用说明

- 实现爬虫
  
  ```scala
  package io.github.wtog.example

  import io.github.wtog.processor.{ Page, PageProcessor, RequestSetting }
  import scala.concurrent.duration._

  /**
    * @author : tong.wang
    * @since : 5/16/18 11:42 PM
    * @version : 1.0.0
    */
  final case class BaiduPageProcessor() extends PageProcessor {

    override def process(page: Page): Unit = {
      // 处理爬去结果
      page.addPageResultItem(Map("title" -> page.title))
      // 添加新的爬去连接
      //    page.addTargetRequest("http://www.baidu.com")
    }

    override def requestSetting: RequestSetting = {
      RequestSetting(
        domain = "www.baidu.com",
        headers = Map("Content-Type" -> "text/html; charset=GB2312"),
        sleepTime = 3 seconds,
        useProxy = true)
    }

    override def targetUrls: List[String] = List("http://www.baidu.com")

    override def cronExpression: Option[String] = Some("*/30 * * ? * *")
  }
  ```
  
- sbt

  1. sbt assembly # 打 jar 包
  2. java -jar target/scala-2.12/web-crawler-assembly.jar

- docker

  1. build image
  
    ```docker
    docker build -f docker/Dockerfile -t web-crawler:0.1.0 .
    ```
    
  2. start container
  
    ```docker
    docker run -it --init --name web-crawler web-crawler:0.1.0 java -jar /apps/web-crawler.jar
    ```
