# Module 2 Demo

First let's package our JAR with our functions!

```bash
mvn package
```

Now we can actually deploy our first function to Pulsar.


```bash
pulsar-admin functions create \ 
--jar target/functions-0.1.0.jar \ 
--classname com.pluralsight.functions.WordCountFunction \ 
--tenant public \
--namespace default \
--name word-count \
--inputs persistent://public/default/sentences \
--output persistent://public/default/count
```

We can verify that it was actually created if we use the Pulsar Admin CLI

```bash
➜ pulsar-admin functions list
"word-count"
```

Let's see if this works! We will create a consumer with a text suscription and try to produce a message.

```bash
pulsar-client produce persistent://public/default/sentences --messages "Hello how are you. Hello indeed"
pulsar-admin functions status --name word-count
```

Awesome!