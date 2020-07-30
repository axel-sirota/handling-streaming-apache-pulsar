package com.pluralsight.functions;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

public class RoutingFunction implements Function<String, Void> {

    private List fruits = Arrays.asList("apple", "orange", "pear", "other fruits...");
    private List vegetables = Arrays.asList("carrot", "lettuce", "radish", "other vegetables...");

    private boolean isFruit(String item) {
        return fruits.contains(item);
    }

    private boolean isVegetable(String item) {
        return vegetables.contains(item);
    }

    @Override
    public Void process(String input, Context context) throws Exception {
        Logger LOG = context.getLogger();
        LOG.info(String.format("Got this input: %s", input));
        if ( isFruit(input) ) {
            String fruits_topic = "fruits";
            context.newOutputMessage(fruits_topic, Schema.STRING).value(input).send();
            } else if ( isVegetable(input) ) {
            String vegetables_topic = "vegetables";
            context.newOutputMessage(vegetables_topic, Schema.STRING).value(input).send();
        } else {
            String warning = String.format("The item %s is neither a fruit nor a vegetable", input);
            LOG.warn(warning);
        }
        return null;

    }

}

