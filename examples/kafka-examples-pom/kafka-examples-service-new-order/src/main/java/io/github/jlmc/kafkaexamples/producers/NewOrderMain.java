package io.github.jlmc.kafkaexamples.producers;

import io.github.jlmc.kafkaexamples.commons.producer.KafkaDispatcher;

import java.math.BigDecimal;
import java.util.UUID;

public class NewOrderMain {

    public static final String TOPIC_NAME__ECOMMERCE_NEW_ORDER = "ECOMMERCE_NEW_ORDER";
    private static final String TOPIC_NAME__ECOMMERCE_SEND_EMAIL = "ECOMMERCE_SEND_EMAIL";

    public static void main(String[] args) {
        try (
                KafkaDispatcher<Order> orderKafkaDispatcher = new KafkaDispatcher<>();
                KafkaDispatcher<Email> emailKafkaDispatcher = new KafkaDispatcher<>()
        ) {

            for (int i = 0; i < 10; i++) {

                // the messages are records, because the kafka will saved for a configured time in the kafka server.
                //Disk space can be a problem, so we must configure the kafka server to discard messages, for example:
                // 1. We can save a messages up to a maximum disk space
                // 2. We can save a message for maximum period of time

                final String orderKey = UUID.randomUUID().toString();
                final Order order = new Order(orderKey, "Duke", new BigDecimal("12.34"));

                orderKafkaDispatcher.send(TOPIC_NAME__ECOMMERCE_NEW_ORDER, orderKey, order);

                String emailKey = UUID.randomUUID().toString();
                final Email emailValue = new Email("New Order", "Thank you! We are processing your order: " + orderKey);
                emailKafkaDispatcher.send(TOPIC_NAME__ECOMMERCE_SEND_EMAIL, emailKey, emailValue);

            }
        }

        System.out.println("All messages have been sent..");

    }

}
