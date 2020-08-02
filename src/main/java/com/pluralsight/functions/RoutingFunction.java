package com.pluralsight.functions;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RoutingFunction implements Function<String, Void> {

    @Override
    public Void process(String input, Context context) throws Exception {
        Logger LOG = context.getLogger();
        LOG.info(String.format("Got this input: %s", input));
		Price inputPrice  = new Price(input);
	    String topic = String.format("year-%s", inputPrice.getYear());
	    context.newOutputMessage(topic, Schema.STRING).value(inputPrice.getPrice()).send();
        return null;

    }

    public static void main(String[] args) throws Exception {
        FunctionConfig functionConfig = new FunctionConfig();
        functionConfig.setName("routing-debug");
        functionConfig.setInputs(Collections.singleton("voo"));
        functionConfig.setClassName(RoutingFunction.class.getName());
        functionConfig.setRuntime(FunctionConfig.Runtime.JAVA);
	    functionConfig.setLogTopic("logging-function-logs");

        LocalRunner localRunner = LocalRunner.builder().functionConfig(functionConfig).build();
        localRunner.start(false);
    }

}

class Price {
	private Date date;

	int getYear() {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		return year;
	}

	String getPrice() {
		return String.valueOf(price);
	}

	private float price;

	Price(String priceString) throws ParseException {
		String[] inputList = priceString.split(",");
		date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(inputList[0]);
		price = Float.parseFloat(inputList[1]);
	}
}
