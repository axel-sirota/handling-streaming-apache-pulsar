package com.pluralsight.schema;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

public class ETFProducer {

	public static void main(String[] args) throws PulsarClientException {

		PulsarClient client = PulsarClient.builder()
					                      .serviceUrl("pulsar://localhost:6650")
					                      .build();
		Producer<ETF> producer = client.newProducer(Schema.AVRO(ETF.class))
					                            .topic("pulsar-postgres-jdbc-sink-topic")
					                            .create();
		ETF etfPrice = new ETF("2015-05-26", "151.1564");
		producer.send(etfPrice);
		producer.close();
		client.close();
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
