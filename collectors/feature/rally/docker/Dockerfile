
FROM docker.io/java:openjdk-8-jdk

MAINTAINER Hygieia@capitalone.com

RUN \
  mkdir /hygieia

COPY *.jar /hygieia/
COPY rally-properties-builder.sh /hygieia/
RUN chmod 777 /hygieia/rally-properties-builder.sh
WORKDIR /hygieia

VOLUME ["/hygieia/logs"]

CMD ./rally-properties-builder.sh && \
  java -jar rally-feature-collector*.jar --spring.config.location=/hygieia/hygieia-rally-feature-collector.properties

