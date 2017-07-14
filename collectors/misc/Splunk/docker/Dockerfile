
FROM docker.io/java:openjdk-8-jdk

MAINTAINER Hygieia@capitalone.com

RUN \
  mkdir /hygieia

COPY *.jar /hygieia/
COPY splunk-properties-builder.sh /hygieia/

WORKDIR /hygieia

VOLUME ["/hygieia/logs"]

CMD ./splunk-properties-builder.sh && \
  java -jar splunk-collector*.jar --spring.config.location=/hygieia/hygieia-splunk-collector.properties

