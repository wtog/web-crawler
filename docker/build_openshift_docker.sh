#!/usr/bin/env bash

sbt assembly

docker build -f docker/Dockerfile -t wtnull/web-crawler:0.1.0 .
docker push wtnull/web-crawler:0.1.0

docker build -f docker/Dockerfile_openshift -t wtnull/web-crawler-openshift:latest .
docker push wtnull/web-crawler-openshift:latest

export KUBECONFIG=~/.kube/config_openshift
oc rollout latest dc/web-crawler-openshift

docker_clean_images