FROM alpine:3.7

MAINTAINER wangtong
USER root

RUN apk update
RUN apk fetch openjdk8
RUN apk add openjdk8

RUN ln -s /usr/lib/jvm/default-jvm/bin/jstat /usr/local/bin/jstat
RUN ln -s /usr/lib/jvm/default-jvm/bin/jcmd /usr/bin/jcmd
RUN ln -s /usr/lib/jvm/default-jvm/bin/jstack /usr/bin/jstack

RUN rm openjdk8-8.171.11-r0.apk

ADD target/scala-2.11/web-crawler-assembly*.jar /apps/web-crawler-0.1.0.jar

