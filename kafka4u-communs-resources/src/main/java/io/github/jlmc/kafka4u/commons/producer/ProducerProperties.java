package io.github.jlmc.kafka4u.commons.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

final class ProducerProperties {

    public static Properties defaults() {
        // 1. create kafka properties config
        Properties properties = new Properties();
        // "bootstrap.servers"
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // key.serializer
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // value.serializer
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // acks
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "1");

        return properties;
    }
}
