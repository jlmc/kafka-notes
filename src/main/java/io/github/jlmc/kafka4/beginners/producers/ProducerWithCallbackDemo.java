package io.github.jlmc.kafka4.beginners.producers;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;

import static io.github.jlmc.kafka4.beginners.KafkaConfigs.*;

// 2
public class ProducerWithCallbackDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerWithCallbackDemo.class);

    public static void main(String[] args) {

        // 1. create kafka properties config
        Properties properties = new Properties();
        // "bootstrap.servers"
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, LOCALHOST_9092);
        // key.serializer
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // value.serializer
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // acks
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "1");


        // 2. create a kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        // 3. create a kafka record
        ProducerRecord<String, String> record = new ProducerRecord<>(FIRST_TOPIC, "hello world");


        // 4. send the kafka record to topic in the kafka using the producer
        Future<RecordMetadata> sent = producer.send(record, ProducerWithCallbackDemo::onCompletion);

        // flush data
        producer.flush();
        // flush and close
        producer.close();
    }

    public static void onCompletion(RecordMetadata recordMetadata, Exception e) {
        if (e != null) {
            LOGGER.error("Error while producing", e);
            return;
        }

        LOGGER.info("""
                ---
                Received a new Metadata:
                  Topic: <{}>
                  Partition: <{}>
                  Offset: <{}>
                  Timestamp: <{}> 
                """, recordMetadata.topic(),
                     recordMetadata.partition(),
                     recordMetadata.offset(),
                     recordMetadata.timestamp());
    }
}
