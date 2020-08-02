#!/usr/bin/env bash

if [[ ! -f "/tmp/pulsar-io-jdbc-postgres-2.6.0.nar" ]]; then
    curl -X "GET" "https://archive.apache.org/dist/pulsar/pulsar-2.6.0/connectors/pulsar-io-jdbc-postgres-2.6.0.nar"  -o /tmp/pulsar-io-jdbc-postgres-2.6.0.nar
fi

docker run -d -it --rm \
--name pulsar-postgres \
--network kafka-pulsar \
-p 5432:5432 \
-e POSTGRES_PASSWORD=password \
-e POSTGRES_USER=postgres \
postgres:11

docker cp /tmp/pulsar-io-jdbc-postgres-2.6.0.nar pulsar-kafka-standalone:/pulsar
docker cp configs/jdbcConfig.yaml pulsar-kafka-standalone:/pulsar/conf
docker cp configs/jdbcSchema.yaml pulsar-kafka-standalone:/pulsar/conf
sleep 10
docker exec -it pulsar-kafka-standalone /bin/bash
