package io.github.jlmc.kafkaexamples.commons.producer;

import io.github.jlmc.kafkaexamples.commons.serialization.GsonSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * This class could be called also as Kafka Producer, but the Kafka client api already provide us a class with the same name.
 */
public class KafkaDispatcher<T> implements Closeable {

    private final KafkaProducer<String, T> producer;

    public KafkaDispatcher() {
        // <key type, type of the message>
        this.producer = new KafkaProducer<>(properties());
    }

    public Future<RecordMetadata> send(final String topic, final String key, final T value) {
        final ProducerRecord<String, T> record = new ProducerRecord<>(topic, key, value);

        return producer.send(record, this::onCompletionSubmissionMessage);
    }

    void onCompletionSubmissionMessage(final RecordMetadata data, final Exception e) {
        if (e != null) {
            e.printStackTrace();
            return;
        }

        System.out.printf("Success: %s --- Partition:%s / Offset: %d / Timestamp: %d\n", data.topic(), data.partition(), data.offset(), data.timestamp());

    }

    private Properties properties() {
        var properties = new Properties();

        // Define where our product should connect, where is the kafka running
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Define the serialize that the producer will use to serialize the messages key
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Define the serialize that the producer will use to serialize the messages value
        //properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GsonSerializer.class.getName());



        return properties;
    }

    @Override
    public void close() {
        producer.close();
    }
}
