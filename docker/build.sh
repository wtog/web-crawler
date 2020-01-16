#!/usr/bin/env bash

find crawler-example -type d -iname 'target' | xargs rm -rf

sbt clean assembly

version='latest'

docker build -f docker/Dockerfile -t wtog/web-crawler:${version} ./crawler-example/target/scala-2.12/