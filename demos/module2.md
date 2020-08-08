# Module 2 Demo

## Deploying pulsar Functions

First let's package our JAR with our functions!

```bash
mvn package
```

Now we can actually deploy our first function to Pulsar.

In our application the first thing we need to do is actually split the incoming messages by year to calculate the 
average closing price of the ETF `VOO` by year. This is done by the RoutingFunction.

We will deploy it to the standalone cluster using:

```bash
pulsar-admin functions create \ 
--jar target/functions-0.2.0.jar \ 
--classname com.pluralsight.functions.RoutingFunction \
--name routing \
--inputs voo
```

We can verify that it was actually created if we use the Pulsar Admin CLI

```bash
➜ pulsar-admin functions list
"routing"
```

Let's see if this works! We will create a consumer with a text suscription and try to trigger with a message.

```bash
(in terminal 1) pulsar-client consume year-2010 -s "test1" -n 0 
(in terminal 2) pulsar-admin functions trigger --tenant public --namespace default --name routing --triggerValue "2010-09-14,90.006"
```
Awesome!


However, as we know, the best way to show this works is by producing a real value!

For that we have our Main class that will send some values into the `voo` topic such that the `routing`
function can pick up.

To run this we must do:

```bash
(in terminal 1) pulsar-client consume year-2010 -s "test1" -n 0
mvn exec:java
```

This should say:

```
----- got message -----
key:[null], properties:[], content:90.006
```

However this is not our only task, we wanted to do some averages! For that, we need to deploy the AvgFunction.
The sweet thing is that the code is the same, we just need to wire it in many places (1 per year we are interested in) so we can do:

```bash
for year in 2012 2013 2014 2015 2016 2017; do pulsar-admin functions create --jar target/functions-0.2.0.jar --classname com.pluralsight.functions.AvgFunction --name avg-$year --inputs year-$year --output avg-$year; done
```

And done, in a couple of lines we have our infra ready!

Let's test it out! Instead of just producing some messages, we will feed a CSV with MANY closing values! We will expand on this in the next module, 
but for now we will configure in one line a feeder that will send all the values into the correct topic.

```bash
bash scripts/feed_pulsar.sh
```

This will download the File connector and set the `voo.txt` dataset to feed the voo topic. From there we should start seeing some stats!

If we `run pulsar-admin function stats --name routing` we will see the stats of the function. We can see indeed:

```
➜  ~ pulsar-admin functions stats --name routing
Warning: Nashorn engine is planned to be removed from a future JDK release
{
  "receivedTotal" : 1808,
  "processedSuccessfullyTotal" : 1806,
  "systemExceptionsTotal" : 0,
  "userExceptionsTotal" : 2,
  "avgProcessLatency" : 11.559697150995568,
 # other stuff
```

So indeed this worked! However, how can we debug this? It is not practical to check some stats or check the processed records to see if our functions works.

## Debugging Pulsar Functions

We have 2 main ways of debugging our functions: logs and using breakpoints.

### Logs

Luckily Apache Pulsar couples well with the language log frameworks (`slf4j` logger or `logging` library) 
so if we just instantiate a logger with the context object, we are ready to go:

```java
public Float process(String input, Context context) throws Exception {
		Logger LOG = context.getLogger();
```
This will automatically create a Logger that attaches to the standard logger! So if we later on deploy our function BUT we also add the flag `--log-topic` then any logs will go to this extra topic for debug purposes!

For example, for the routing function, we can do:

```
pulsar-admin functions \
create --jar target/functions-0.2.0.jar \ 
--classname com.pluralsight.functions.RoutingFunction --name routing \
--inputs "voo" --log-topic logging-function-logs
```

So any INFO or upper logs will get printed there, in fact if we rerun the file feeder, you could do:

```bash
(in one terminal ) pulsar-client consume logging-function-logs -s "logs-subscription" -n 0
(other terminal) pulsar-admin sources localrun --archive "$pulsar_home/connectors/pulsar-io-file-2.6.0.nar" --name file-test --destination-topic-name "voo" --source-config-file "/tmp/pluralsight-files/file-connector.yaml"
```

And in the first terminal you will see:

```bash
key:[null], properties:[loglevel=INFO], content:Got this input: 2017-08-01,225.99
----- got message -----
key:[null], properties:[loglevel=INFO], content:Got this input: 2017-08-02,226.12
----- got message -----
key:[null], properties:[loglevel=INFO], content:Got this input: 2017-08-03,225.72
----- got message -----
key:[null], properties:[loglevel=INFO], content:Got this input: 2017-08-04,226.1
----- got message -----
key:[null], properties:[loglevel=INFO], content:Got this input: 2017-08-07,226.47

# etc...
```

So we can actually log with our Pulsar functions!

The other way is using the IDE debugger, because if we deploy in `localrun` mode, we can actually use the IDE to instantiate the function
and therefore we can hook breakpoints as we are used to! This is key to iterate fast!

### Next topics

In next modules we will explore how to feed the topics with another sources (as Kafka) and we will seewhat we can do with the processed records in the new topics as sinks!