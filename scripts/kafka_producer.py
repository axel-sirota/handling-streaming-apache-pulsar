from kafka import KafkaProducer
import sys

topic = sys.argv[1]
producer = KafkaProducer(bootstrap_servers='pulsar-kafka:9092', value_serializer=lambda v: v.encode('utf-8'))
with open("./voo.txt", 'r') as infile:
    for line in infile:
        producer.send(str(topic), line)
