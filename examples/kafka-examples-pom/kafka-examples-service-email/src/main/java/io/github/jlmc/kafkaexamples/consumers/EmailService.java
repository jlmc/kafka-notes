package io.github.jlmc.kafkaexamples.consumers;

import io.github.jlmc.kafkaexamples.commons.consumer.KafkaService;
import io.github.jlmc.kafkaexamples.commons.serialization.GsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public class EmailService {

    public static final String TOPIC_NAME = "ECOMMERCE_SEND_EMAIL";

    public static void main(String[] args) {

        final Map<String, String> overrideProperties = Map.of(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName(),
                GsonDeserializer.TYPE_CONFIG, Email.class.getName());

        final EmailService emailService = new EmailService();

        try (KafkaService<Email> kafkaService = new KafkaService<>(
                EmailService.class.getSimpleName(),
                TOPIC_NAME,
                emailService::parse,
                overrideProperties)) {

            kafkaService.run();
        }
    }

    void parse(ConsumerRecord<String, Email> record) {

        System.out.println("---------------------------------");

        System.out.printf("Sending new Email: checking for fraud \n" +
                        " -- key: %s \n" +
                        "-- Message: %s\n" +
                        "-- Partition: %d\n" +
                        "-- offset: %d \n"
                , record.key(), record.value(), record.partition(), record.offset());

        //VeryLongProcess.runProcess(1000L);

        System.out.println("--- Email Processed processed");
    }
}
