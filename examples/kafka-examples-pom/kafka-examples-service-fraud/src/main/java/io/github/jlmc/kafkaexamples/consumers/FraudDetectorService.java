package io.github.jlmc.kafkaexamples.consumers;

import io.github.jlmc.kafkaexamples.commons.consumer.KafkaService;
import io.github.jlmc.kafkaexamples.commons.serialization.GsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public class FraudDetectorService {

    public static final String TOPIC_NAME = "ECOMMERCE_NEW_ORDER";

    public static void main(String[] args) {

        final Map<String, String> overrideProperties = Map.of(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName(),
                GsonDeserializer.TYPE_CONFIG, Order.class.getName());

        final FraudDetectorService fraudDetectorService = new FraudDetectorService();

        try (KafkaService<Order> kafkaService = new KafkaService<>(
                FraudDetectorService.class.getSimpleName(),
                TOPIC_NAME,
                fraudDetectorService::analise,
                overrideProperties)) {

            kafkaService.run();
        }
    }

    private void analise(ConsumerRecord<String, Order> record) {
        System.out.printf(
                "Processing new Order: checking for fraud\n" +
                        "    -- key: %s\n" +
                        "    -- Message: %s\n" +
                        "    -- Partition: %d\n" +
                        "    -- offset: %d\n"
                , record.key(), record.value(), record.partition(), record.offset());

        //VeryLongProcess.runProcess(1500L);

        System.out.println("--- Order processed");
    }


}
