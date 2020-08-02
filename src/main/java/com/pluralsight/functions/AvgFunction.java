package com.pluralsight.functions;

import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class AvgFunction implements Function<String, Float> {
	private static Charset charset = Charset.forName("UTF-8");
	@Override
	public Float process(String input, Context context) throws Exception {
		Logger LOG = context.getLogger();
		String key = context.getInputTopics().toString().replace("[", "").replace("]", "");
		int num_measurements = update_measurements(context, key);
		LOG.info(String.format("The number of measurements is %s", num_measurements));
		float old_average = get_old_average(context, key);
		float new_average = (old_average  * (num_measurements-1) + Float.parseFloat(input)) / num_measurements;
		update_state(context, key, new_average);
		LOG.info(String.format("The average for %s is : %s", key, new_average));
		return new_average;
	}

	private void update_state(Context context, String key, float new_average) {
		String new_average_in_string = Float.toString(new_average);
		context.putState(key, ByteBuffer.wrap(new_average_in_string.getBytes(charset)));
	}

	private float get_old_average(Context context, String key) {
		Logger LOG = context.getLogger();
		ByteBuffer old_state = context.getState(key);
		String old_average = "0";
		if (old_state != null) {
			try {
				old_average = StandardCharsets.UTF_8.decode(old_state).toString();
				LOG.info(String.format("Got state for key %s and the old average is %s", key, old_average));
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		}
		if (old_average.equals("")) {
			old_average = "0";
		}

		return Float.parseFloat(old_average);
	}

	private int update_measurements(Context context, String key) {
		context.incrCounter(String.format("num-measurements-%s", key), 1);
		return Math.toIntExact(context.getCounter(String.format("num-measurements-%s", key)));
	}

	public static void main(String[] args) throws Exception {
		FunctionConfig functionConfig = new FunctionConfig();
		functionConfig.setName("avg-2011-debug");
		functionConfig.setInputs(Collections.singleton("year-2011"));
		functionConfig.setClassName(AvgFunction.class.getName());
		functionConfig.setRuntime(FunctionConfig.Runtime.JAVA);
		functionConfig.setOutput("avg-2011");
		functionConfig.setLogTopic("logging-function-logs");
		LocalRunner localRunner = LocalRunner.builder()
				                          .functionConfig(functionConfig)
				                          .build();
		localRunner.start(false);
	}
}