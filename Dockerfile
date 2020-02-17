FROM openjdk:11
MAINTAINER Maris Locmelis

WORKDIR /java/
COPY out/artifacts/mysql_concurrency_test_jar/mysql_concurrency_test.jar mysql_concurrency_test.jar

CMD java -classpath mysql_concurrency_test.jar test.Executor
