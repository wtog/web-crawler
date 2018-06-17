FROM alpine:3.7

MAINTAINER wangtong
USER root

RUN apk update
RUN apk fetch openjdk8
RUN apk add openjdk8

ADD target/scala-2.12/*.jar /apps/

