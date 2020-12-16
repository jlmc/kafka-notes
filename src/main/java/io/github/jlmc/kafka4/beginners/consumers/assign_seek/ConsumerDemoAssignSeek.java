package io.github.jlmc.kafka4.beginners.consumers.assign_seek;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static io.github.jlmc.kafka4.beginners.KafkaConfigs.FIRST_TOPIC;
import static io.github.jlmc.kafka4.beginners.KafkaConfigs.LOCALHOST_9092;

/**
 * 4.Assign and seek are mostly used to replay data or fetch a specific message.
 *
 * Read only the 5 messages in a specific partition topic after a specific offset
 *
 * This time we are not using the "group id"
 */
public class ConsumerDemoAssignSeek {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerDemoAssignSeek.class);

    public static final String APP_NAME = "App4";

    public static void main(String[] args) {

        // 1. create kafka properties config
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, LOCALHOST_9092);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, APP_NAME);
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

        TopicPartition partitionToReadFrom = new TopicPartition(FIRST_TOPIC, 0);
        long offsetToReadFrom = 15L;

        // 3. Assign and seek are mostly used to replay data or fetch a specific message
        consumer.assign(List.of(partitionToReadFrom));

        // 4. seek
        consumer.seek(partitionToReadFrom, offsetToReadFrom);


        int numberOfMessagesToRead = 5;
        boolean keepOnReading = true;



        // 4. poll for new data
        for (int numberOfReadMessagesSoFar = 0; keepOnReading;) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records) {
                LOGGER.info("--- Key: <{}> Value: <{}> Partition: <{}>  Offset: <{}>",
                        record.key(),
                        record.value(),
                        record.partition(),
                        record.offset());

                numberOfReadMessagesSoFar++;
                if (numberOfReadMessagesSoFar >= numberOfMessagesToRead) {
                    keepOnReading = false;
                    break;
                }
            }

            LOGGER.info("EXITING THE APPLICATION!!!");
        }

    }

}
