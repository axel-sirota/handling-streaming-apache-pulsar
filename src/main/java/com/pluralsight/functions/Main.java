package com.pluralsight.functions;

import org.apache.pulsar.client.api.*;

public class Main {
    public static void main(String[] args) throws PulsarClientException {

        PulsarClient client = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();
        Producer<byte[]> producer = client.newProducer()
                .topic("voo")
                .create();
        String[] messages = new String[]{"2010-09-14,90.006", "2011-12-02,101.22", "2012-08-30,115.77", "2012-09-25,119.5", "2014-06-13,165.7"};

        // You can then send messages to the broker and topic you specified:

        for (String message: messages) {
            producer.send(message.getBytes());
        }

        System.out.println("Messages sent!\n");

    }
}
