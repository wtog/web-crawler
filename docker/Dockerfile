FROM alpine:3.8

MAINTAINER wangtong
USER root

RUN echo http://mirrors.ustc.edu.cn/alpine/v3.8/main > /etc/apk/repositories && \
    echo http://mirrors.ustc.edu.cn/alpine/v3.8/community >> /etc/apk/repositories

RUN apk update && apk upgrade

RUN apk fetch openjdk8
RUN apk add openjdk8

RUN ln -s /usr/lib/jvm/default-jvm/bin/jstat /usr/local/bin/jstat
RUN ln -s /usr/lib/jvm/default-jvm/bin/jcmd /usr/bin/jcmd
RUN ln -s /usr/lib/jvm/default-jvm/bin/jstack /usr/bin/jstack

RUN rm -rf /openjdk8*

ADD target/scala-2.12/web-crawler-assembly.jar /apps/web-crawler.jar

CMD ["java", "-jar", "/apps/web-crawler.jar"]

