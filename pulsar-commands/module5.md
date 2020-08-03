# Migrating from Kafka to Pulsar

Chances are you already have a production app that is writing to Apache Kafka, so is painful to plan a migration.
The most common application of Kafka is actually to get logs from applications (it mayb be logs with a kafka dumper or directly log files)
and fetch those into topics, so later it can be used by some application; usually `kafka-dump` that sends that into a datastore.

Pulsar has the Kafka adapter for Java applications that enables KafkaProducers to send the messages into Pulsar topics, 
enabling a migration (or maintain both if you need) with 0 code change.

## Setup

What we will review is an application that uses the `log4j` kafka appender to send all application logs into a Kafka topic, 
and we will migrate that into a Pulsar topic.

The first thing we need is to package our application and send it to the `kafka-pulsar`container:

```bash
mvn clean package
docker cp target/functions-0.2.0-jar-with-dependencies.jar pulsar-kafka:/functions-0.2.0-jar-with-dependencies.jar
docker cp files voo.txt pulsar-kafka:/
```

We must send the "fat jar" because we need dependencies to run on its own.


## Testing the application

Let's test if this works!

```bash
docker exec -it pulsar-kafa /bin/bash
mkdir -p /tmp/pluralsight-kafka-migrator
mv voo.txt /tmp/pluralsight-kafka-migrator
java -jar functions-0.2.0-jar-with-dependencies.jar
```

We will see in the console this type of logs:

```bash
06:12:02.197 [main] INFO  kafka - 2011-01-24,103.22
06:12:02.197 [main] INFO  kafka - 2011-01-25,103.18
06:12:02.197 [main] INFO  kafka - 2011-01-26,103.72
06:12:02.198 [main] INFO  kafka - 2011-01-27,103.86
06:12:02.198 [main] INFO  kafka - 2011-01-28,101.97
06:12:02.198 [main] INFO  kafka - 2011-01-31,102.82
06:12:02.198 [main] INFO  kafka - 2011-02-01,104.4
06:12:02.198 [main] INFO  kafka - 2011-02-02,104.25
06:12:02.198 [main] INFO  kafka - 2011-02-03,104.5
```

So now we must consume the Kafka topic and check if the logs are there!

```bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic log-test --from-beginning --partition 0
```

And we see:

```
{
  "timeMillis" : 1596433071010,
  "thread" : "main",
  "level" : "INFO",
  "loggerName" : "kafka",
  "message" : "2017-11-09,237.18",
  "endOfBatch" : false,
  "loggerFqcn" : "org.apache.logging.slf4j.Log4jLogger",
  "threadId" : 1,
  "threadPriority" : 5
}

{
  "timeMillis" : 1596433071012,
  "thread" : "main",
  "level" : "INFO",
  "loggerName" : "kafka",
  "message" : "2017-11-10,237.04",
  "endOfBatch" : false,
  "loggerFqcn" : "org.apache.logging.slf4j.Log4jLogger",
  "threadId" : 1,
  "threadPriority" : 5
}
```

Success! The key in the application was on the configuration of the `src/main/resources/log4j2.xml` file.

## Migrating the application

The migration, actually is super simple! The only thing we need to change is the maven dependency and the log handlers!

On the `pom.xml`

We will change:

```
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>2.5.0</version>
        </dependency>
```

with

```
        <dependency>
			<groupId>org.apache.pulsar</groupId>
			<artifactId>pulsar-client-kafka</artifactId>
			<version>2.6.0</version>
		</dependency>
```

And the Pulsar team has already taken care of all the adapting! On the log handler side, in the file `src/main/resources/log4j2.xml`:

```
        <Kafka name="Kafka" topic="log-test">
            <JsonLayout />
            <Property name="bootstrap.servers">localhost:9092</Property>
        </Kafka>
```

Gets changes to:

```
        <Kafka name="Kafka" topic="persistent://public/default/log-test">
            <JsonLayout />
            <Property name="bootstrap.servers">localhost:6650</Property>
        </Kafka>
```

And done!!

Let's test it!!

```bash
mvn clean package
docker cp target/functions-0.2.0-jar-with-dependencies.jar pulsar-kafka-standalone:/functions-0.2.0-jar-with-dependencies.jar
docker cp files voo.txt pulsar-kafka-standalone:/pulsar
```

Note that it really doesn't matter which container we run this, since localhost is the same for the network

In one terminal we will consume the `log-test` topic:

```bash
pulsar-client consume -s "kafka-test" -n 0 log-test
```

And in another terminal let's run this!

```bash
docker exec -it pulsar-kafka-standalone /bin/bash
mkdir -p /tmp/pluralsight-kafka-migrator
mv voo.txt /tmp/pluralsight-kafka-migrator
java -jar functions-0.2.0-jar-with-dependencies.jar
```

And we can see on the topic:

```
```

Success! It is **that** easy to migrate to Pulsar!! And regarding the kafka-dump, well we know how to do that, since it is just a sink! So we deploy it *no problemo*!



