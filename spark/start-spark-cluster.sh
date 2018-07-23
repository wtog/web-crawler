#!/usr/bin/env bash

# start cluster
./sbin/start-all.sh

# submit application to spark
./bin/spark-submit \
    --class org.apache.spark.examples.streaming.NetworkWordCount \
    --name "YourAppNameHere" \
    --master spark://doc.local:7077 \
    --driver-memory 1G \
    --conf spark.executor.memory=1g \
    --conf spark.cores.max=100 \
    examples/target/original-spark-examples_2.11-2.4.0-SNAPSHOT.jar 127.0.0.1 8066