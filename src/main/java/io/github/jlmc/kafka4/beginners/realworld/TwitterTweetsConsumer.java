package io.github.jlmc.kafka4.beginners.realworld;

import io.github.jlmc.kafka4.beginners.KafkaConfigs;
import io.github.jlmc.kafka4.beginners.consumers.thread.KafkaConsumerService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 3
 */
public class TwitterTweetsConsumer {

    public static final String TWITTER_TWEETS = "twitter_tweets";

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTweetsConsumer.class);

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        KafkaConsumerService service =
                KafkaConsumerService.newConsumer(
                        KafkaConfigs.LOCALHOST_9092,
                        TwitterTweetsConsumer.class.getSimpleName(),
                        TWITTER_TWEETS,
                        TwitterTweetsConsumer::consume,
                        Map.of(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1"));

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
