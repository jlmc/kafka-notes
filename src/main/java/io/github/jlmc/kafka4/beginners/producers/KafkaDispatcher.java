package io.github.jlmc.kafka4.beginners.producers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import static io.github.jlmc.kafka4.beginners.KafkaConfigs.LOCALHOST_9092;

public class KafkaDispatcher<V> implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaDispatcher.class);

    private final KafkaProducer<String, V> producer;

    private KafkaDispatcher(KafkaProducer<String, V> producer) {
        this.producer = producer;
    }

    public static <V> KafkaDispatcher<V> newKafkaDispatcher() {
        return newKafkaDispatcher(Collections.emptyMap());
    }

    public static <V> KafkaDispatcher<V> newKafkaDispatcher(Map<String, String> properties) {
        Properties allProperties = defaults();
        allProperties.putAll(properties);

        KafkaProducer<String, V> producer = new KafkaProducer<>(allProperties);
        return new KafkaDispatcher<>(producer);
    }

    private static Properties defaults() {
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

        return properties;
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

        LOGGER.debug("Received a new Metadata: Topic: <{}>  Partition: <{}> Offset: <{}> Timestamp: <{}>",
                recordMetadata.topic(),
                recordMetadata.partition(),
                recordMetadata.offset(),
                recordMetadata.timestamp());
    }
}
