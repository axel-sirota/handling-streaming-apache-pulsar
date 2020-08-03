# Inspecting topics with Pulsar SQL

Let's face it, sometimes things in distributed systems just don't work. In that case is where
you need the tooling around your system to be able to debug easily! We have learned along the course several ways of 
debugging our deployable artifacts, but Pulsar has also a Presto cluster to literally query all messages!

## Setup

Pulsar SQL  is a Presto deployment that allows to fetch metadata and all messages from all objects in Pulsar.

The first thing we need is to launch the Presto server:

```bash
docker exec pulsar-kafka-standalone bin/pulsar sql-worker start
```

After this, hopefully we can get into the `pulsar-kafka-standalone` container and start a presto console to run our queries!

## Testing the connection

Let's start a presto session and run some queries:

```bash
bin/pulsar sql
```

If everything worked fine, we should be able to run the following query:

```
presto> show catalogs;
 Catalog
---------
 pulsar
 system
(2 rows)
```

Inside the `pulsar` catalog we can get all schemas:

```
presto> show schemas in pulsar;
        Schema
-----------------------
 information_schema
 public/default
 public/functions
 sample/standalone/ns1
(4 rows)
```

As we already know, we have 4 namespaces by default:
- Our own
- The one for functions
- 2 for Pulsar internal

We can also get the topics in the `public/default` namespace:

```
presto> show tables in pulsar."public/default";
              Table
---------------------------------
 avg-2010
 avg-2011
 avg-2012
 avg-2013
 avg-2014
 avg-2015
 avg-2016
 avg-2017
 kafka-test-topic
 logging-function-logs
 pulsar-postgres-jdbc-sink-topic
 voo
 year-2010
 year-2011
 year-2012
 year-2013
 year-2014
 year-2015
 year-2016
 year-2017
(20 rows)
```

Which are all the topics we used alongside the course!

Let's query `voo` and see what we find:

```
presto> SHOW COLUMNS FROM pulsar."public/default"."voo";
      Column       |   Type    | Extra |                                   Comment
-------------------+-----------+-------+-----------------------------------------------------------------------------
 __value__         | varchar   |       | The value of the message with primitive type schema
 __partition__     | integer   |       | The partition number which the message belongs to
 __event_time__    | timestamp |       | Application defined timestamp in milliseconds of when the event occurred
 __publish_time__  | timestamp |       | The timestamp in milliseconds of when event as published
 __message_id__    | varchar   |       | The message ID of the message used to generate this row
 __sequence_id__   | bigint    |       | The sequence ID of the message used to generate this row
 __producer_name__ | varchar   |       | The name of the producer that publish the message used to generate this row
 __key__           | varchar   |       | The partition key for the topic
 __properties__    | varchar   |       | User defined properties
(9 rows)

Query 20200803_000600_00001_cns7i, FINISHED, 1 node
Splits: 19 total, 19 done (100.00%)
0:02 [9 rows, 1.13KB] [3 rows/s, 504B/s]
```

We can check that every topic is a table in Presto, and we have all the metadata associated with the Pulsar topic:

- The message value
- The properties
- The keys
- The ID
- Etc...

We can virtually debug ANY issue with Pulsar SQL, making it a really powerful tool!

To wrap up, let's also check the topic ` pulsar-postgres-jdbc-sink-topic`:

```
presto> SHOW COLUMNS FROM pulsar."public/default"."pulsar-postgres-jdbc-sink-topic";
      Column       |   Type    | Extra |                                   Comment
-------------------+-----------+-------+-----------------------------------------------------------------------------
 date              | varchar   |       | field can be null
 price             | varchar   |       | field can be null
 __partition__     | integer   |       | The partition number which the message belongs to
 __event_time__    | timestamp |       | Application defined timestamp in milliseconds of when the event occurred
 __publish_time__  | timestamp |       | The timestamp in milliseconds of when event as published
 __message_id__    | varchar   |       | The message ID of the message used to generate this row
 __sequence_id__   | bigint    |       | The sequence ID of the message used to generate this row
 __producer_name__ | varchar   |       | The name of the producer that publish the message used to generate this row
 __key__           | varchar   |       | The partition key for the topic
 __properties__    | varchar   |       | User defined properties
(10 rows)

Query 20200803_005627_00003_zq62p, FINISHED, 1 node
Splits: 19 total, 19 done (100.00%)
0:01 [10 rows, 1.45KB] [13 rows/s, 1.99KB/s]

```

This means that if we have a schema in our topic, that schema is reflected in our table and we query over it!

## Connecting the dots

Let's go back to the previous module to see a practical use case to this tool. 

Let's imagine we try to feed a *new file* `voo_2.txt` into our Kafka deployment that will, in effect trigger all our Pulsar Functions, what will happen?

```bash
docker cp scripts/kafka_producer.py pulsar-kafka-standalone:/pulsar/
docker cp files/voo_2.txt pulsar-kafka-standalone:/pulsar/
docker exec -it pulsar-kafka-standalone /bin/bash
python3 kafka_producer.py "voo" "voo_2.txt"
```

If we check the source `kafka-test`, we can see it executed correctly:

```bash
bin/pulsar-admin sources status --name kafka-test
```

However:

```
# bin/pulsar-admin functions status --name routing
{
  "numInstances" : 1,
  "numRunning" : 1,
  "instances" : [ {
    "instanceId" : 0,
    "status" : {
      "running" : true,
      "error" : "",
      "numRestarts" : 0,
      "numReceived" : 1619,
      "numSuccessfullyProcessed" : 1617,
      "numUserExceptions" : 2,
      "latestUserExceptions" : [ {
        "exceptionString" : "1",
        "timestampMs" : 1596416699228
      }, {
        "exceptionString" : "1",
        "timestampMs" : 1596416759346
      } ],
      "numSystemExceptions" : 0,
      "latestSystemExceptions" : [ ],
      "averageLatency" : 11.938211775787499,
      "lastInvocationTime" : 1596416759846,
      "workerId" : "c-standalone-fw-localhost-8080"
    }
  } ]
}
```

We had some exceptions! Of course, we could delete the deployment and debug it in localrun, but if this is production that is not such a simple decision!

Also, we can check in the log-topic, however the issue there is that we need to have an open suscription in the topic **before** running these, so we are still in no good place...

Let's try to find another way!

Let's do the following query:

```
presto> SELECT __value__ FROM pulsar."public/default"."voo" WHERE __value__ NOT LIKE '%,%';
    __value__
------------------
 2013-06-03137.34+

 2013-06-03137.34+

(2 rows)
```

Wow! It appears that some of our messages where badly formatted! Therefore when the Routing function tried to parse the date
it failed! This would have been super hard to debug from the Pulsar Function side! A good idea to prevent this from happening from now on is setting a Schema to ensure that we can actually parse a date from the first elements.
