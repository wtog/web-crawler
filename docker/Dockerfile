FROM alpine:latest

MAINTAINER wangtong
USER root

RUN apk update
RUN apk fetch openjdk8
RUN apk add openjdk8

RUN ln -s /usr/lib/jvm/default-jvm/bin/jstat /usr/local/bin/jstat
RUN ln -s /usr/lib/jvm/default-jvm/bin/jcmd /usr/bin/jcmd
RUN ln -s /usr/lib/jvm/default-jvm/bin/jstack /usr/bin/jstack

RUN rm -rf /openjdk8*

ADD target/scala-2.12/web-crawler-assembly*.jar /apps/web-crawler-0.1.0.jar

