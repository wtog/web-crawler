#!/usr/bin/env bash

find target -type f -iname '*assembly*jar' | xargs rm

sbt assembly

jar_name=`find target -type f -iname '*assembly*jar'`
version_with_jar="${jar_name#*assembly-}"
version="${version_with_jar%%.jar}"

docker build -f docker/Dockerfile -t wtog/web-crawler:${version} .
docker push wtog/web-crawler:${version}

docker images -a | awk '/none/ {print $3}' | xargs docker rmi --force