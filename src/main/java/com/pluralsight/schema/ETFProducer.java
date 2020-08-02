package com.pluralsight.schema;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class ETFProducer {

	private static Logger log = Logger.getLogger("logger");

	public static void main(String[] args) throws PulsarClientException {

		PulsarClient client = null;
		try {
			client = PulsarClient.builder()
					                      .serviceUrl("pulsar://localhost:6650")
					                      .build();
		} catch (PulsarClientException e) {
			e.printStackTrace();
			System.exit(1);
		}
		Producer<ETF> producer = null;
		log.info(new String(Schema.AVRO(ETF.class).getSchemaInfo().getSchema()));
		try {
			producer = client.newProducer(Schema.AVRO(ETF.class))
					                            .topic("pulsar-postgres-jdbc-sink-topic")
					                            .create();
		} catch (PulsarClientException e) {
			e.printStackTrace();
			System.exit(1);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		ETF etfPrice = new ETF("2015-05-26", "151.1564");
		try {
			producer.send(etfPrice);
		} catch (PulsarClientException e) {
			System.out.println("Messages not sent!");
			e.printStackTrace();
		} finally {
			producer.close();
			client.close();
		}

		System.out.println("Messages sent!\n");
	}
}

class ETF {

	private String date;
	private String price;

	public ETF(String inputDate, String inputPrice) {
		date=inputDate;
		price=inputPrice;
	}

	public ETF() {
		date="2010-01-01";
		price="100";
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}
}
