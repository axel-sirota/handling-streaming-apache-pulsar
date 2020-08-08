# Connecting sources to Pulsar

A message bus like Pulsar is as good as it can be easily connected with the outside world! In this demo we will see how to deploy *sources*.

As an example we will have a Kafka deployment with Docker in our laptops and we will deploy a **connector** that will automatically transport messages from the Kafka topic _into_ the Apache Pulsar Topic.

## Setup

For the setup, then, we need a Kafka Deployment (which involves a Zookeeper deployment too) and an Apache 
Pulsar deployment  that effectively can connect to the Kafka Broker (in terms of network connectivity). The best idea to get that
is to deploy everything with Docker (at least locally!) within a private docker network.

We will start by running (ensure no Pulsar is running from the previous module!):

```bash
bash scripts/configure_kafka.sh
```

This will, in order:

- Download the kafka client and connector
- Create the kafka-pulsar network
- Deploy the 3 services within that network (with proper ports exposed)
- Copy relevant files to the `pulsar-kafka-standalone` container (the one running Apache Pulsar)
- Login to the `pulsar-kafka-standalone` container 

## Testing the connection

Now the only thing we need to do is to deploy the connector. As a connector is just a _special_ type of Pulsar Function, we know how to do this!

```bash
bin/pulsar-admin sources create --archive "./pulsar-io-kafka-2.6.0.nar"  \
--classname org.apache.pulsar.io.kafka.KafkaBytesSource \
--name kafka-test --destination-topic-name "kafka-test-topic" \
--source-config-file "./conf/kafkaTestConfig.yaml" \
--tenant public --namespace default
```

Let's test it! The easiest way to do this is to publish a message to the Kafka topic and consume it in the Pulsar connected topic.

```bash
(in one terminal within the container) bin/pulsar-client consume "kafka-test-topic" -s "kafka-subscription" -n 0
(in the other terminal) pip install kafka-python && python3 kafka_producer.py "kafka-test-topic"
```

Success! We should see:

```
----- got message -----
key:[null], properties:[], content:2017-10-06,233.68

----- got message -----
key:[null], properties:[], content:2017-10-09,233.27

----- got message -----
key:[null], properties:[], content:2017-10-10,233.86

----- got message -----
key:[null], properties:[], content:2017-10-11,234.24

16:04:28.703 [pulsar-timer-4-1] INFO  org.apache.pulsar.client.impl.ConsumerStatsRecorderImpl - [kafka-test-topic] [kafka-subscription] [b48b0] Prefetched messages: 0 --- Consume throughput received: 29.70 msgs/s --- 0.00 Mbit/s --- Ack sent rate: 29.70 ack/s --- Failed messages: 0 --- batch messages: 0 ---Failed acks: 0
```

Meaning that effectively we got the messages!

## Connecting the dots

Now the only thing left is ensuring that our previous module 2 Pulsar Functions are deployed and reconfigure the source to point to the new topic.

This is easy! To verify the pulsar functions we can check with `bin/pulsar-admin functions list`. Depending on how you mounted the Pulsar docker 
image you may or may not have them. To redeploy them is as easy as:

```bash
docker cp target/functions-0.2.0.jar pulsar-kafka-standalone:/pulsar
docker exec -it pulsar-kafka-standalone /bin/bash
bin/pulsar-admin functions create --jar ./functions-0.2.0.jar --classname com.pluralsight.functions.RoutingFunction --name routing --inputs "voo" --log-topic logging-function-logs
for year in 2010 2011 2012 2013 2014 2015 2016 2017; do bin/pulsar-admin functions create --jar ./functions-0.2.0.jar --classname com.pluralsight.functions.AvgFunction --name avg-$year --inputs year-$year --log-topic logging-function-logs --output avg-$year; done
```

And the `pulsar-admin` CLI on the sources subcommand it accepts an update flag to resubmit another config file (which is where we defined the Kafka topic)! So we must do:

```bash
bin/pulsar-admin sources update \
--name kafka-test --destination-topic-name "voo" \
--source-config-file "./conf/kafkaSourceConfig.yaml" \
--tenant public --namespace default
```

And done! If we run the `kafka_producer.py` form the container with the new topic we should populate all the corresponding topics!
Keep in mind that on next modules we will see how to adapt Pulsar to existing Kafka Deployments that will have production data, so we can 
have them in parallel for a time until we decide to do the switch!

```bash
(in one terminal within the container) bin/pulsar-client consume "avg-2010" -s "kafka-integration-subscription" -n 0
(in the other terminal) pip install kafka-python && python3 kafka_producer.py "voo"
```

We should see on the topic `"avg-2010"` something like:

```bash
key:[null], properties:[__pfn_input_msg_id__=CIkBEOIBIAA=, __pfn_input_topic__=persistent://public/default/year-2010], content:95.35861
----- got message -----
key:[null], properties:[__pfn_input_msg_id__=CIkBEOMBIAA=, __pfn_input_topic__=persistent://public/default/year-2010], content:95.38129
----- got message -----
key:[null], properties:[__pfn_input_msg_id__=CIkBEOQBIAA=, __pfn_input_topic__=persistent://public/default/year-2010], content:95.40416
----- got message -----
key:[null], properties:[__pfn_input_msg_id__=CIkBEOUBIAA=, __pfn_input_topic__=persistent://public/default/year-2010], content:95.42615
----- got message -----
key:[null], properties:[__pfn_input_msg_id__=CIkBEOYBIAA=, __pfn_input_topic__=persistent://public/default/year-2010], content:95.44842
```

Which is the actual average closing price for the ETF VOO in the year `2010`! 