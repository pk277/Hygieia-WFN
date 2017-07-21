
FROM docker.io/java:openjdk-8-jdk

MAINTAINER Hygieia@capitalone.com

RUN \
  mkdir /hygieia

COPY *.jar /hygieia/
COPY hygieia-splunk-properties-builder.sh /hygieia/
RUN chmod 777 /hygieia/hygieia-splunk-properties-builder.sh
WORKDIR /hygieia

VOLUME ["/hygieia/logs"]

CMD ./hygieia-splunk-properties-builder.sh && \
  java -jar hygieia-splunk-collector*.jar --spring.config.location=/hygieia/hygieia-splunk-collector.properties

