package io.github.jlmc.kafka4u.commons.consumer;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

class ConsumerProperties {

    public static Properties defaults() {
        // 1. create kafka properties config
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
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


        // Define the max pull records in the poll method,
        // this will hell in the balancing decreasing commit time
        // this a very common configuration in a very large company, market leaders
        //properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

        return properties;
    }

    public static Properties with(String bootstrapServer, String groupId) {
        Properties properties = defaults();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return properties;
    }

    private ConsumerProperties() {
        throw new AssertionError();
    }
}

