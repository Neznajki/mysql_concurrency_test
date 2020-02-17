#!/bin/bash

docker build -t neznajki/mysql-concurrency-test .
docker push neznajki/mysql-concurrency-test:latest
