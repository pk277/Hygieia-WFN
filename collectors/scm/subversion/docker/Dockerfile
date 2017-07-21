
FROM docker.io/java:openjdk-8-jdk

MAINTAINER Hygieia@capitalone.com

RUN \
  mkdir /hygieia

COPY *.jar /hygieia/
COPY subversion-properties-builder.sh /hygieia/
RUN chmod 777 /hygieia/subversion-properties-builder.sh
WORKDIR /hygieia

VOLUME ["/hygieia/logs"]

CMD ./subversion-properties-builder.sh && \
  java -jar subversion-collector*.jar --spring.config.location=/hygieia/hygieia-subversion-collector.properties

