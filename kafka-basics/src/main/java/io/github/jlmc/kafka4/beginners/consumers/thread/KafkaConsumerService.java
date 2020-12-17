package io.github.jlmc.kafka4.beginners.consumers.thread;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public final class KafkaConsumerService implements Runnable, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerService.class);

    //@formatter:off
    public static final Consumer<ConsumerRecord<String, String>> EMPTY_CONSUMER_RECORD = r -> {};
    //@formatter:on

    private final CountDownLatch latch;
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final String topic;

    private final Consumer<ConsumerRecord<String, String>> chain;


    private KafkaConsumerService(KafkaConsumer<String, String> kafkaConsumer,
                                 String topic,
                                 Consumer<ConsumerRecord<String, String>> chain,
                                 CountDownLatch latch) {
        this.latch = latch;
        this.topic = topic;
        this.chain = chain;
        this.kafkaConsumer = kafkaConsumer;
    }

    public static KafkaConsumerService newConsumer(String bootstrapServer,
                                                   String groupId,
                                                   String topic,
                                                   Consumer<ConsumerRecord<String, String>> chain,
                                                   Map<String, String> overrideProperties) {

        Properties properties = ConsumerProperties.with(bootstrapServer, groupId);
        properties.putAll(overrideProperties);

        KafkaConsumer<String, String > kafkaConsumer = new KafkaConsumer<>(properties);

        return new KafkaConsumerService(kafkaConsumer, topic, chain, new CountDownLatch(1));

    }

    public static KafkaConsumerService newConsumer(String bootstrapServer,
                                                   String groupId,
                                                   String topic,
                                                   Consumer<ConsumerRecord<String, String>> chain) {

        return newConsumer(bootstrapServer, groupId, topic, chain, new CountDownLatch(1));

    }

    public static KafkaConsumerService newConsumer(String bootstrapServer,
                                                    String groupId,
                                                    String topic,
                                                    Consumer<ConsumerRecord<String, String>> chain,
                                                    CountDownLatch latch) {

        Properties properties = ConsumerProperties.with(bootstrapServer, groupId);
        KafkaConsumer<String, String > kafkaConsumer = new KafkaConsumer<>(properties);

        return new KafkaConsumerService(kafkaConsumer, topic, chain, latch);
    }

    @Override
    public void run() {
        kafkaConsumer.subscribe(Collections.singleton(this.topic));

        try {
            while (true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
                consume(records);
            }
        } catch (WakeupException e) {

            LOGGER.error("Error WakeupException the consumer ", e);

        } finally {
            this.kafkaConsumer.close();
            // tell to the main code that we are done with the consumer
            this.latch.countDown();
        }
    }

    @Override
    public void close() {
        // wakeup is a special method to interrupt consumer.Poll()
        // it may throw the exception  org.apache.kafka.common.errors.WakeupException
        // tis method is thread-safe and is useful in particular to abort a long poll
        this.kafkaConsumer.wakeup();

        //this.kafkaConsumer.close();
    }

    public void await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.error("Service got interrupted", e);
        } finally {
            LOGGER.info("Service is closing");
        }
    }

    private void consume(ConsumerRecords<String, String> records) {
        for (ConsumerRecord<String, String> record : records) {
            LOGGER.info("-- Key: <{}> Value: <{}> Partition: <{}> Offset: <{}>",
                    record.key(), record.value(), record.partition(), record.offset());

            consume(record);
        }
    }

    private void consume(ConsumerRecord<String, String> record) {
        if (this.chain == null) {
            return;
        }

        this.chain.accept(record);
    }

}
