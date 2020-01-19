#!/usr/bin/env bash

wget -O chromedriver_linux64.zip -q https://npm.taobao.org/mirrors/chromedriver/79.0.3945.36/chromedriver_linux64.zip

unzip -uxo chromedriver_linux64.zip -d /opt

chmod +x /opt/chromedriver

rm chromedriver_linux64.zip

sbt ';clean ;coverage ;test ;coverageReport'
