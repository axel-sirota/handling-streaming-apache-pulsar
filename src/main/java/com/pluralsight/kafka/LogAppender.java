package com.pluralsight.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class LogAppender {

	private static final Logger log = LoggerFactory.getLogger("kafka");
	private static final String viewDirectory = "/tmp/pluralsight-kafka-migrator";
	static Set<File> filesViewed = new TreeSet<File>() {};

	public static void main(String... args) throws InterruptedException, IOException {
		while (true) {
			File[] files = new File(viewDirectory).listFiles();
			showFiles(files);
			Thread.sleep(10);
		}
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
            log.info(line);
		}
	}
}
