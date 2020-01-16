#!/usr/bin/env bash

set -x

wget -q https://npm.taobao.org/mirrors/chromedriver/80.0.3987.16/chromedriver_linux64.zip

unzip -x chromedriver_linux64.zip -d /opt

sbt ';clean ;coverage ;test ;coverageReport'

rm chromedriver_linux64.zip
