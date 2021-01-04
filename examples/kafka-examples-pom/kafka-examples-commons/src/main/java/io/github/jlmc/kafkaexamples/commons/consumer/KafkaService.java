package io.github.jlmc.kafkaexamples.commons.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class KafkaService<T> implements Closeable {

    private final String groupId;
    private final ConsumerFunction<T> consumerFunction;
    private final KafkaConsumer<String, T> consumer;
    private final AtomicBoolean isRunning;

    private KafkaService(final String groupId,
                         final ConsumerFunction<T> consumerFunction,
                         final Map<String, String> overrideProperties) {
        this.isRunning = new AtomicBoolean(false);
        this.groupId = groupId;
        this.consumerFunction = consumerFunction;
        final Properties properties = getProperties(overrideProperties);
        this.consumer = new KafkaConsumer<>(properties);
    }

    public KafkaService(final String groupId,
                        final String topic,
                        final ConsumerFunction<T> consumerFunction,
                        final Map<String, String> overrideProperties) {
        this(groupId, consumerFunction, overrideProperties);
        this.consumer.subscribe(Collections.singleton(topic));
    }

    public KafkaService(final String groupId,
                        final Pattern topic,
                        final ConsumerFunction<T> consumerFunction,
                        final Map<String, String> overrideProperties) {
        this(groupId, consumerFunction, overrideProperties);
        this.consumer.subscribe(topic);
    }

    public void run() {
        isRunning.set(true);

        // we can put a consumer Listening in multiple topic, however, this is not a practice used by many teams.
        // Because the code gets very confused
        // Another reason: the the single responsibility principle, Each service have a single responsibility, so it will Listening in a single Topic
        //this.consumer.subscribe(topicNames);

        // we need consumer forever
        while (isRunning.get()) {

            final ConsumerRecords<String, T> records = consumer.poll(Duration.ofMillis(100));

            if (!records.isEmpty()) {
                System.out.printf("Found %d Records... \n", records.count());
            }

            for (final ConsumerRecord<String, T> record : records) {

                this.consumerFunction.consume(record);

            }
        }
    }

    @Override
    public void close() {
        stop();
        this.consumer.close();
    }

    private void stop() {
        this.isRunning.set(false);
        consumer.unsubscribe();
    }


    private Properties getProperties(Map<String, String> overrideProperties) {
        final Properties properties = properties();
        properties.putAll(overrideProperties);
        return properties;
    }

    private Properties properties() {
        var properties = new Properties();

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // each consumer should have a group, the group is used to balanced the work, we a two consumer have the same group they will share the work
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);

        //  // the consumer id should id unique
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());

        // Define the max pull records in the poll method,
        // this will hell in the balancing decreasing commit time
        // this a very common configuration in a very large company, market leaders
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

        return properties;
    }

}
