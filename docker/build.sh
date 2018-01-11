#!/bin/bash
REPO=docker-reg.emotibot.com.cn:55688
REPO=${REPO_JENKINS:-$REPO}

CONTAINER=multiroundTemplateTest
CONTAINER=${CONTAINER_JENKINS:-$CONTAINER}

TAG=$(git rev-parse --short HEAD)
DOCKER_IMAGE=$REPO/$CONTAINER:$TAG

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

BUILDROOT=$DIR/../

cmd="docker build --no-cache \
  -t $DOCKER_IMAGE \
  -f $DIR/Dockerfile \
  $BUILDROOT"
echo $cmd
eval $cmd