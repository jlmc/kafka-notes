package io.github.jlmc.kafkaexamples.consumers;

import io.github.jlmc.kafkaexamples.commons.consumer.KafkaService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Consume all records of topics names with regex pattern: ECOMMERCE.*
 */
public class LoggerService {

    public static final Pattern TOPIC_NAME = Pattern.compile("ECOMMERCE.*");

    public static void main(String[] args) {

        final LoggerService loggerService = new LoggerService();

        try (KafkaService<String> kafkaService = new KafkaService<>(
                LoggerService.class.getSimpleName(),
                TOPIC_NAME,
                loggerService::trace,
                Collections.emptyMap())) {

            kafkaService.run();
        }
    }

    private static Properties properties() {
        var properties = new Properties();

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // each consumer should have a group, the group is used to balanced the work, we a two consumer have the same group they will share the work
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, LoggerService.class.getSimpleName());

        // Define the max pull records in the poll method,
        // this will hell in the balancing decreasing commit time
        // this a very common configuration in a very large company, market leaders
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

        return properties;
    }

    private void trace(ConsumerRecord<String, String> record) {
        System.out.printf("" +
                        "Logging new Email: checking for fraud\n" +
                        "    -- topic: %s\n" +
                        "    -- key: %s\n" +
                        "    -- Message: %s\n" +
                        "    -- Partition: %d\n" +
                        "    -- offset: %d\n"
                , record.topic(), record.key(), record.value(), record.partition(), record.offset());

        //veryLongProcess();

        System.out.println("--- Logging Processed processed");
    }
}
