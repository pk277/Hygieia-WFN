
FROM docker.io/java:openjdk-8-jdk

MAINTAINER Hygieia@capitalone.com

RUN \
  mkdir /hygieia

COPY *.jar /hygieia/
COPY jenkins-build-properties-builder.sh /hygieia/
RUN chmod 777 /hygieia/jenkins-build-properties-builder.sh
WORKDIR /hygieia

VOLUME ["/hygieia/logs"]

CMD ./jenkins-build-properties-builder.sh && \
  java -jar jenkins-build-collector*.jar --spring.config.location=/hygieia/hygieia-jenkins-build-collector.properties

