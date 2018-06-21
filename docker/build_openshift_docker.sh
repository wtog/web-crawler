#!/usr/bin/env bash

sbt assembly

docker build -f docker/Dockerfile -t wtnull/web-crawler:0.1.0 .
docker push wtnull/web-crawler:0.1.0

docker build -f docker/Dockerfile_openshift -t wtnull/web-crawler-openshift:0.1.0 .
docker push wtnull/web-crawler-openshift:0.1.0
