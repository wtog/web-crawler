#!/usr/bin/env bash

set -e

base=$(basename $(pwd))

if [ "$base" == "bin" ]; then
  cd ../
fi

sbt 'project example; assembly'

echo "java -jar crawler-example/target/scala-2.12/web-crawler-assembly.jar $* > /tmp/crawler.log 2>&1 &"
echo "crawler starting..."
nohup java -jar crawler-example/target/scala-2.12/web-crawler-assembly.jar $* > /tmp/crawler.log 2>&1 &
