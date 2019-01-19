# web-crawler    
[![Build Status](https://travis-ci.org/wtog/web-crawler.svg?branch=master)](https://travis-ci.org/wtog/web-crawler.svg?branch=master)


#### 项目介绍
参考 webmagic [http://webmagic.io](http://webmagic.io) 撸的 [scala + akka] 爬虫

#### 使用说明

- 实现爬虫
  
      package io.github.wtog.example

      import io.github.wtog.processor.{ Page, PageProcessor, RequestHeaders }

      /**
        * @author : tong.wang
        * @since : 5/16/18 11:42 PM
        * @version : 1.0.0
        */

      final case class BaiduPageProcessor() extends PageProcessor {

        override def process(page: Page): Unit = {
          val document = page.jsoupParser

          // 处理爬去结果
          page.addPageResultItem(Map("title" -> document.title()))
          // 添加新的爬去连接
          page.addTargetRequest("http://www.baidu.com")
        }

        override def requestHeaders: RequestHeaders = {
          RequestHeaders(
            domain = "www.baidu.com",
            commonHeaders = Map("Content-Type" -> "text/html; charset=GB2312"), useProxy = true)
        }

        override def targetUrls: List[String] = {
          List("http://www.baidu.com")
        }
      }


- sbt

	  1. sbt assembly # 打 jar 包
	  2. java -jar target/scala-2.12/web-crawler-0.1.0.jar


- docker

	1. build image
    	- docker build -f docker/Dockerfile -t web-crawler:0.1.0 .

  2. start container
		- docker run -it --init --name web-crawler web-crawler:0.1.0 java -jar /apps/web-crawler-0.1.0.jar

