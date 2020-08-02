#!/bin/bash

if [[ -z "$pulsar_home" ]]; then
    printf "Please set the $pulsar_home variable"
    exit 1
fi
if [[ -z "$repo_location" ]]; then
    printf "Please set the $repo_location variable"
    exit 1
fi
mkdir -p "$pulsar_home/connectors"
curl -X "GET" "https://archive.apache.org/dist/pulsar/pulsar-2.6.0/connectors/pulsar-io-file-2.6.0.nar"  -o $pulsar_home/connectors/pulsar-io-file-2.6.0.nar
mkdir -p "/tmp/pluralsight-files"
cp configs/file-connector.yaml /tmp/pluralsight-files
cp files/voo.txt /tmp/pluralsight-files
pulsar-admin sources localrun --archive "$pulsar_home/connectors/pulsar-io-file-2.6.0.nar" --name file-test --destination-topic-name "voo" --source-config-file "/tmp/pluralsight-files/file-connector.yaml"
