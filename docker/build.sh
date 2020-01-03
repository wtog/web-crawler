#!/usr/bin/env bash

find target -type f -iname '*assembly*jar' | xargs rm

sbt assembly

jar_name=`find target -type f -iname '*assembly*jar'`

version='latest'

docker build -f docker/Dockerfile -t wtog/web-crawler:${version} .