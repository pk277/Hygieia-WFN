
FROM docker.io/java:openjdk-8-jdk

MAINTAINER Hygieia@capitalone.com

RUN \
  mkdir /hygieia

COPY *.jar /hygieia/
COPY sonar-codequality-properties-builder.sh /hygieia/
RUN chmod 777 /hygieia/sonar-codequality-properties-builder.sh
WORKDIR /hygieia

VOLUME ["/hygieia/logs"]

CMD ./sonar-codequality-properties-builder.sh && \
  java -jar sonar-codequality-collector*.jar --spring.config.location=/hygieia/hygieia-sonar-codequality-collector.properties

