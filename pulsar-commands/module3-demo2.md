# Connecting sinks to Pulsar

As we know, message buses usually are the midman between high traffic producers and the final database! In this demo we will see how to deploy *sinks* which take care of this.

Sinks are Pulsar functions that are specific to integrate with several known databases (Files, MongoDB, ElasticSearch, MariaDB, etc...). 
As an example we will have a Postgres deployment with Docker in our laptops and we will deploy a **jdbc connector** that will automatically transport messages from the Pulsar topic _into_ 
the Postgres table, **taking into account the table schema**.

## Setup

For the setup, then, we need a Postgres Deployment and an Apache 
Pulsar deployment  that effectively can connect to the Database. The best idea to get that
is to deploy everything with Docker (at least locally!) within a private docker network. In fact, we will reuse our 
previous network so later on we can connect everything.

We will start by running:

```bash
bash scripts/configure_postgres.sh
```

This will, in order:

- Download the jdbc-postgres connector
- Deploy the database within that network (with proper ports exposed)
- Copy relevant files to the `pulsar-kafka-standalone` container (the one running Apache Pulsar)
- Login to the `pulsar-kafka-standalone` container 

Now we need to create the corresponding table with a specific schema:

```
psql -h pulsar-postgres -p 5432 -U postgres pulsar_postgres_jdbc_sink

create table if not exists pulsar_postgres_jdbc_sink
(
date VARCHAR(255) NOT NULL,    
price VARCHAR(255) NOT NULL    
);

exit
```

Now that we have our table, we can deploy our sink! Still inside our container we will submit the schema of our table into the topic (they must match!)

```bash
bin/pulsar-admin schemas upload pulsar-postgres-jdbc-sink-topic -f ./conf/jdbcSchema.yaml
bin/pulsar-admin sinks create \
--archive ./pulsar-io-jdbc-postgres-2.6.0.nar \
--inputs pulsar-postgres-jdbc-sink-topic \
--name pulsar-postgres-jdbc-sink \
--sink-config-file ./conf/jdbcConfig.yaml \
--parallelism 1
```

We can verify this was OK because we will get the sink information with the admin cli:

```bash
bin/pulsar-admin sinks get \
--tenant public \
--namespace default \
--name pulsar-postgres-jdbc-sink
```

Now, effectively, we:

- Deployed a Postgres database and created the corresponding table
- Submitted the table schema to the connected Apache Pulsar topic
- Deployed the **sink connector** that connected both.

Let's test it!

## Testing the connection

This should be easy! We must produce a message into the topic `pulsar-postgres-jdbc-sink-topic` with the corresponding schema
and later on, verify it is actually in the Postgres table. Let's do it!

The only difference with our different cases, is that now we are submitting a **Schema** and Apache Pulsar will be enforcing it in the server side.

So, how do we produce a message with a Schema? Until now we were always treating the whole message as a `string`!

We have already a Schema-aware produce, the key is setting in the Producer creation the type of the Class of message:

```java
Producer<ETF> producer = client.newProducer(Schema.AVRO(ETF.class))
				                            .topic("pulsar-postgres-jdbc-sink-topic")
				                            .create();
```

So now is as simple as running this! We also adapted the `pom.xml` to allow multiple entrypoint so is easier to keep pur previous Main class!

```bash
mvn exec:java@second-cli
```

Now we should be able to see the message in the database!

```bash
docker exec -it pulsar-postgres /bin/bash
psql -U postgres
show * from pulsar_postgres_jdbc_sink;
```

Success! We should see:

```
pulsar_postgres_jdbc_sink=# select * from pulsar_postgres_jdbc_sink;
    date    |  price
------------+----------
 2015-05-26 | 151.1564
```
