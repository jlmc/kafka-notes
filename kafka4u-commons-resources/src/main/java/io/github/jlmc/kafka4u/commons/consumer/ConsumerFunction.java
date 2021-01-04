package io.github.jlmc.kafka4u.commons.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

@FunctionalInterface
public interface ConsumerFunction<V> {

    void consume(ConsumerRecord<String, V> consumerRecord);

}
