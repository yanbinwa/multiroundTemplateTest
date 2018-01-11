#!/bin/bash
# NOTE:
# DO NOT touch anything outside <EDIT_ME></EDIT_ME>,
# unless you really know what you are doing.

REPO=docker-reg.emotibot.com.cn:55688
# The name of the container, should use the name of the repo is possible
# <EDIT_ME>
CONTAINER=multiroundTemplateTest
# </EDIT_ME>

# Load env file
# Load env file
source $1
if [ "$#" -ne 2 ];then
  echo "Erorr, can't open envfile: $1"
  echo "Usage: $0 <envfile> <tags>"
  echo "e.g., $0 dev.env 3384387"
  exit 1
else
  envfile=$1
  TAG=$2
  echo "# Using envfile: $envfile"
fi

DOCKER_IMAGE=$REPO/$CONTAINER:$TAG

echo "DOCKER_IMAGE: $DOCKER_IMAGE"

# global config:
# - use local timezone
# - max memory = 5G/10G
# - restart = always
globalConf="
  -v /etc/localtime:/etc/localtime \
  -m 20G \
  --restart always \
"

# <EDIT_ME>

# Get runroot
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
RUNROOT=$DIR/../
echo "RUNROOT : $RUNROOT"
# Compose module config
moduleConf="
  -p $MTT_HOST_SERVER_PORT:$MTT_SERVER_PORT \
  --env-file $envfile \
"
# </EDIT_ME>

docker rm -f -v $CONTAINER
cmd="docker run -d --name $CONTAINER \
  $globalConf \
  $moduleConf \
  $DOCKER_IMAGE \
"
echo $cmd
eval $cmd