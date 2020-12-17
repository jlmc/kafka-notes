package io.github.jlmc.kafka4.beginners.consumers.thread;

import io.github.jlmc.kafka4.beginners.KafkaConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 3
 */
public class ExampleApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleApp.class);

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        KafkaConsumerService service =
                KafkaConsumerService.newConsumer(
                        KafkaConfigs.LOCALHOST_9092,
                        ExampleApp.class.getSimpleName(),
                        KafkaConfigs.FIRST_TOPIC,
                        ExampleApp::consume);

        //service.run();

        System.out.println("....");

        executorService.submit(service);

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(() -> {
                            LOGGER.info("Trying Shutdown the application...");
                            service.close();
                            service.await();
                            LOGGER.info("Shutdown successful, Received close signal!");

                        }));

        service.await();
    }

    private static void consume(ConsumerRecord<String, String> record) {
        System.out.printf("--> Key: <%s> Value: <%s> Partition: <%s> Offset: <%s>%n",
                record.key(), record.value(), record.partition(), record.offset());
    }


}
