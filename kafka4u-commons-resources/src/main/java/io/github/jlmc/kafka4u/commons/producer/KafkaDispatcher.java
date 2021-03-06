package io.github.jlmc.kafka4u.commons.producer;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;


public class KafkaDispatcher<V> implements AutoCloseable {
    public static final String LOCALHOST_9092 = "localhost:9092";

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaDispatcher.class);

    private final KafkaProducer<String, V> producer;

    private KafkaDispatcher(KafkaProducer<String, V> producer) {
        this.producer = producer;
    }

    public static <V> KafkaDispatcher<V> newKafkaDispatcher() {
        return newKafkaDispatcher(Collections.emptyMap());
    }

    public static <V> KafkaDispatcher<V> newKafkaDispatcher(Map<String, String> properties) {
        Properties allProperties = ProducerProperties.defaults();
        allProperties.putAll(properties);

        KafkaProducer<String, V> producer = new KafkaProducer<>(allProperties);
        return new KafkaDispatcher<>(producer);
    }


    public Future<RecordMetadata> send(String topic, String key, V value) {
        ProducerRecord<String, V> record = new ProducerRecord<>(topic, key, value);

        return producer.send(record, this::onCompletion);
    }

    @Override
    public void close() {
        // flush data
        producer.flush();
        // flush and close
        producer.close();
    }

    private void onCompletion(RecordMetadata recordMetadata, Exception e) {
        if (e != null) {
            LOGGER.error("Error while producing", e);
            return;
        }

        LOGGER.info("Received a new Metadata: Topic: <{}>  Partition: <{}> Offset: <{}> Timestamp: <{}>",
                recordMetadata.topic(),
                recordMetadata.partition(),
                recordMetadata.offset(),
                recordMetadata.timestamp());
    }
}

