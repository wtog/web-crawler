# web-crawler

#### 项目介绍
参考 webmagic [http://webmagic.io](http://webmagic.io) 撸的 [scala + akka] 爬虫


#### 使用说明
    - scala

        - sbt assembly # 打 jar 包

        ```
            java -jar target/scala-2.12/web-crawler-0.1.0.jar
        ```

    - docker

        build image
        - docker build -f docker/Dockerfile -t web-crawler:0.1.0 .

        start container
        - docker run -it --init --name web-crawl web-crawler:0.1.0 java -jar /apps/web-crawler-0.1.0.jar

