package io.github.jlmc.kafka4u.commons.consumer;

import org.apache.kafka.clients.consumer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * <pre>
 *        KafkaService<String> kafkaService =
 *                 KafkaService.newKafkaService(
 *                         "localhost:9092",
 *                         "twitter_tweets",
 *                         KafkaConsumerElasticsearchDispatcher.class.getSimpleName());
 *
 *         try (consumer; kafkaService) {
 *             kafkaService.addListener(consumer::consumeRecord);
 *             kafkaService.run();
 *         }
 *
 *         Runtime.getRuntime().addShutdownHook(new Thread(kafkaService::stopConsuming));
 *
 *         System.out.println("Goodbye...");
 *
 * </pre>
 *
 * @see <a href="https://docs.confluent.io/3.0.1/clients/javadocs/org/apache/kafka/clients/consumer/KafkaConsumer.html">https://docs.confluent.io/3.0.1/clients/javadocs/org/apache/kafka/clients/consumer/KafkaConsumer.html</a>
 */
public class KafkaService<V> implements AutoCloseable, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final boolean autoCommit;
    private final String topic;
    private final KafkaConsumer<String, V> kafkaConsumer;
    private final List<ConsumerFunction<V>> listeners = new ArrayList<>();

    private KafkaService(String topic, KafkaConsumer<String, V> kafkaConsumer, boolean autoCommit) {
        this.topic = topic;
        this.kafkaConsumer = kafkaConsumer;
        this.autoCommit = autoCommit;
    }

    public static <V> KafkaService<V> newKafkaService(String bootstrapServer,
                                                      String topic,
                                                      String groupId) {
        return newKafkaService(bootstrapServer, topic, groupId, Collections.emptyMap());
    }

    public static <V> KafkaService<V> newKafkaService(String bootstrapServer,
                                                      String topic,
                                                      String groupId,
                                                      Map<String, String> overrideProperties) {

        Properties configs = ConsumerProperties.with(bootstrapServer, groupId);
        configs.putAll(overrideProperties);

        boolean autoCommit = Boolean.parseBoolean(configs.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"));

        KafkaConsumer<String, V> kafkaConsumer = new KafkaConsumer<>(configs);
        return new KafkaService<V>(topic, kafkaConsumer, autoCommit);
    }


    @Override
    public void run() {
        running.set(true);

        // we can put a consumer Listening in multiple topic, however, this is not a practice used by many teams.
        // Because the code gets very confused
        // Another reason: the the single responsibility principle, Each service have a single responsibility, so it will Listening in a single Topic
        try {
            kafkaConsumer.subscribe(List.of(topic));

            // we need consumer forever
            while (running.get()) {

                final ConsumerRecords<String, V> records = kafkaConsumer.poll(Duration.ofMillis(100));

                if (!records.isEmpty()) {
                    LOGGER.debug("Found <{}> Records... \n", records.count());
                }

                try {
                    consumeRecords(records);
                } catch (Exception e) {

                    // TODO: 19/12/2020 - think in retry mechanism to process the records offsets that fails
                    if (!autoCommit) {
                        
                        LOGGER.error("Error happens processing records <{}> because is auto commit disable the offset will not be committed", e.getMessage(), e);
                    } else {
                        throw e;
                    }
                }


            }

        } finally {
            kafkaConsumer.unsubscribe();
        }
    }

    private void consumeRecords(ConsumerRecords<String, V> records) {
        if (listeners.isEmpty() || records.isEmpty()) {
            return;
        }

        for (ConsumerRecord<String, V> record : records) {

            LOGGER.debug("Delegating to the Listeners: Key: <{}> Value: <{}> Partition: <{}> Offset: <{}>",
                    record.key(), record.value(), record.partition(), record.offset());

            for (ConsumerFunction<V> listener : listeners) {
                listener.consume(record);
            }
        }

        if (!autoCommit) {
            LOGGER.info("Committing the Offsets...");
            kafkaConsumer.commitSync();
            LOGGER.info("Offsets have been committed...");
        }
    }

    @Override
    public void close() {
        running.set(false);
        this.kafkaConsumer.close();
    }


    public KafkaService<V> addListener(ConsumerFunction<V> consume) {
        this.listeners.add(consume);
        return this;
    }

    public void stopConsuming() {
        running.set(false);
    }
}
