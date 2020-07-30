package com.pluralsight.functions;

import org.apache.pulsar.client.api.*;

public class Main {
    public static void main(String[] args) throws PulsarClientException {

        PulsarClient client = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();
        Producer<byte[]> producer = client.newProducer()
                .topic("basket-items")
                .create();
        String[] messages = new String[]{"apple", "radish", "patri es genial", "%&$#^*&@$"};

        // You can then send messages to the broker and topic you specified:

        for (String message: messages) {
            producer.send(message.getBytes());
        }

        System.out.println("Messages sent!\n");

        Consumer fruitsConsumer = client.newConsumer()
                .topic("fruits")
                .subscriptionName("fruits-subscription")
                .subscribe();
        Consumer vegetableConsumer = client.newConsumer()
                .topic("vegetables")
                .subscriptionName("vegetables-subscription")
                .subscribe();

        int fruitCount = 0;
        while (fruitCount < 1) {
            // Wait for a message
            Message msg = fruitsConsumer.receive();

            try {
                // Do something with the message
                System.out.printf("Message received: %s\n", new String(msg.getData()));

                // Acknowledge the message so that it can be deleted by the message broker
                fruitsConsumer.acknowledge(msg);
            } catch (Exception e) {
                // Message failed to process, redeliver later
                fruitsConsumer.negativeAcknowledge(msg);
            }
            fruitCount++;

        }
        int vegetableCount = 0;
        while (vegetableCount  < 1) {
            // Wait for a message
            Message msg = vegetableConsumer.receive();

            try {
                // Do something with the message
                System.out.printf("Message received: %s\n", new String(msg.getData()));

                // Acknowledge the message so that it can be deleted by the message broker
                vegetableConsumer.acknowledge(msg);
            } catch (Exception e) {
                // Message failed to process, redeliver later
                vegetableConsumer.negativeAcknowledge(msg);
            }
            vegetableCount++;

        }

    }
}
