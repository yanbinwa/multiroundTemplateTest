#FROM maven:3.3.9-jdk-8
FROM docker-reg.emotibot.com.cn:55688/base/java8:20160809

# Set TimeZone
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# set bundle version
ENV BUNDLE_VER=1.0-SNAPSHOT

# setup workspace
RUN mkdir -p /usr/src/multiroundTemplateTest/
RUN mkdir -p /usr/src/multiroundTemplateTest/log
RUN mkdir -p /usr/src/multiroundTemplateTest/file
WORKDIR /usr/src/multiroundTemplateTest

COPY bin ./bin
COPY conf ./conf
COPY docker ./docker

# Download main jar
COPY target/multiroundTemplateTest-1.0-SNAPSHOT.jar /usr/src/multiroundTemplateTest

# defaults
ENTRYPOINT ["bin/run.sh"]