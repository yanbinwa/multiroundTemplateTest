#!/bin/bash
#
# Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
# EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
#
# Primary Owner: zuobai@emotibot.com.cn
# The template comes from EM-ctr-docker (seanchuang@emotibot.com.cn)
#

# Prepare config file

java -Dfile.encoding=UTF8 \
     -Dlog4j.configuration="file:conf/log4j.properties" \
     -DCONFIG_PATH="conf/env" \
     -server \
     -Xms2g -Xmx10g \
     -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled \
     -XX:MaxMetaspaceSize=6g \
     -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 \
     -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark \
     -jar multiroundTemplateTest-1.0-SNAPSHOT.jar