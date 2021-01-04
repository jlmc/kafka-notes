package io.github.jlmc.kafkaexamples.commons.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

@FunctionalInterface
public interface ConsumerFunction<T> {

    void consume(ConsumerRecord<String, T> consumerRecord);

}
