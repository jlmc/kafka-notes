package io.github.jlmc.kafka4.beginners.consumers.groups;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static io.github.jlmc.kafka4.beginners.KafkaConfigs.FIRST_TOPIC;
import static io.github.jlmc.kafka4.beginners.KafkaConfigs.LOCALHOST_9092;

public class ConsumerDemoAppTwo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerDemoAppTwo.class);

    public static final String APP_NAME = "App2";

    public static void main(String[] args) {

        // 1. create kafka properties config
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, LOCALHOST_9092);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, APP_NAME);
        // What to do when there is no initial offset in Kafka or if the current
        // offset does not exist any more on the server
        // (e.g. because that data has been deleted):
        // earliest: automatically reset the offset to the earliest offset
        // latest: automatically reset the offset to the latest offset<
        // none: throw exception to the consumer if no previous offset is found for the consumer's group<
        // anything else: throw exception to the consumer.;
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 2. create a consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // 3. subscribe a consumer to our topic
        consumer.subscribe(Collections.singleton(FIRST_TOPIC));

        // 4. poll for new data
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            printoutRecords(records);
        }

    }

    private static void printoutRecords(ConsumerRecords<String, String> records) {
        for (ConsumerRecord<String, String> record : records) {

            LOGGER.info("""
                    ---
                    Key: <{}>
                    Value: <{}>
                    Partition: <{}>
                    Offset: <{}>
                    """, record.key(),
                         record.value(),
                         record.partition(),
                         record.offset());


            record.key();

        }
    }
}
