#!/usr/bin/env bash

if [[ ! -f "/tmp/kafka-clients-0.10.2.1.jar" ]]; then
    curl -X "GET" "https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/0.10.2.1/kafka-clients-0.10.2.1.jar"  -o /tmp/kafka-clients-0.10.2.1.jar
fi
if [[ ! -f "/tmp/pulsar-io-kafka-2.6.0.nar" ]]; then
    curl -X "GET" "https://archive.apache.org/dist/pulsar/pulsar-2.6.0/connectors/pulsar-io-kafka-2.6.0.nar"  -o /tmp/pulsar-io-kafka-2.6.0.nar
fi
docker network create kafka-pulsar
docker run -d -it -p 2181:2181 --name pulsar-kafka-zookeeper --network kafka-pulsar wurstmeister/zookeeper
docker run -d -it --network kafka-pulsar -p 6667:6667 -p 9092:9092 -e KAFKA_ADVERTISED_HOST_NAME=pulsar-kafka -e KAFKA_ZOOKEEPER_CONNECT=pulsar-kafka-zookeeper:2181 --name pulsar-kafka wurstmeister/kafka:2.11-1.0.2
docker run -d -it --network kafka-pulsar -p 6650:6650 -p 8080:8080 -v $PWD/data:/pulsar/data --name pulsar-kafka-standalone apachepulsar/pulsar:2.6.0 bin/pulsar standalone

docker cp /tmp/pulsar-io-kafka-2.6.0.nar pulsar-kafka-standalone:/pulsar
docker cp configs/kafkaSourceConfig.yaml pulsar-kafka-standalone:/pulsar/conf
docker cp configs/kafkaTestConfig.yaml pulsar-kafka-standalone:/pulsar/conf
docker cp /tmp/kafka-clients-0.10.2.1.jar pulsar-kafka-standalone:/pulsar/lib
docker cp scripts/kafka_producer.py pulsar-kafka-standalone:/pulsar/
docker cp files/voo.txt pulsar-kafka-standalone:/pulsar/

docker exec -it pulsar-kafka-standalone /bin/bash
