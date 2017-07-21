
FROM docker.io/java:openjdk-8-jdk

MAINTAINER Hygieia@capitalone.com


ENV SPRING_DATA_MONGODB_DATABASE=dashboarddb
ENV SPRING_DATA_MONGODB_HOST=11.16.42.153
ENV SPRING_DATA_MONGODB_PORT=27017
ENV SPRING_DATA_MONGODB_USERNAME=dashboarduser
ENV SPRING_DATA_MONGODB_PASSWORD=dbpassword


RUN \
  mkdir /hygieia

COPY hygieia/ /hygieia
COPY properties-builder.sh /hygieia/
RUN chmod 777 /hygieia/properties-builder.sh

WORKDIR /hygieia

VOLUME ["/hygieia/logs"]


EXPOSE 8080
CMD ./properties-builder.sh &&\
  java -Djava.security.egd=file:/dev/./urandom -jar api.jar --spring.config.location=/hygieia/dashboard.properties
#CMD java -jar api.jar
