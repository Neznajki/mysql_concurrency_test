FROM openjdk:11
MAINTAINER Maris Locmelis

WORKDIR /
ADD dynatech-challange.jar dynatech-challange.jar
ADD testData.json testData.json

ENV DATA_FILE=testData.json
ENV RESULT_FILE=/tmp/results.json

CMD java -jar dynatech-challange.jar
