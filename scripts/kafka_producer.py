from kafka import KafkaProducer
import sys

topic = sys.argv[1]
if len(sys.argv) > 2:
    file = sys.argv[2]
else:
    file = "./voo.txt"
producer = KafkaProducer(bootstrap_servers='pulsar-kafka:9092', value_serializer=lambda v: v.encode('utf-8'))
with open(file, 'r') as infile:
    for line in infile:
        producer.send(str(topic), line)
