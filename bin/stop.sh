#!/usr/bin/env bash

ps -ef |grep web-crawler-assembly | awk '{print $2}' | xargs kill -9