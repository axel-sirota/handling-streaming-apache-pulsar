package com.pluralsight.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class LogAppender {

	private static final Logger log = LoggerFactory.getLogger("kafka");
	private static final String viewDirectory = "/tmp/pluralsight-kafka-migrator";
	static Set<File> filesViewed = new TreeSet<File>() {};
	private static KafkaProducer<Object, Object> producer;
	private static String topic;
	private static String broker;

	public static void main(String... args) throws InterruptedException, IOException {
		configure_producer();
		while (true) {
			File[] files = new File(viewDirectory).listFiles();
			showFiles(files);
			Thread.sleep(10);
		}
	}

	private static void configure_producer() {
		Properties prop = new Properties();
		String fileName = "config.properties";
		InputStream inputStream = LogAppender.class.getClassLoader().getResourceAsStream(fileName);
		try {
			assert inputStream != null;
			prop.load(inputStream);
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		topic = prop.getProperty("topic");
		broker = prop.getProperty("broker");
		log.info("Read broker {} and topic {}", broker, topic);
		Properties props = new Properties();
		props.put("bootstrap.servers", broker);
		props.put("key.serializer", IntegerSerializer.class.getName());
		props.put("value.serializer", StringSerializer.class.getName());

		producer = new KafkaProducer<>(props);
	}

	private static void showFiles(File[] files) throws IOException {
		for (File file : files) {
			if (! file.isDirectory()) {
				if ( ! filesViewed.contains(file) ) {
					System.out.println(String.format("Got file %s", file));
					processFile(file);
					filesViewed.add(file);
				}
			} else {
				showFiles(Objects.requireNonNull(file.listFiles()));
			}
		}
	}

	private static void processFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = br.readLine()) != null)
		{
			producer.send(new ProducerRecord<>(topic, line));
			log.info("Message {} sent successfully", line);
		}
	}
}
