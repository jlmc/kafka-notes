package io.github.jlmc.kafka4u.es;

import io.github.jlmc.kafka4u.commons.consumer.KafkaService;
import io.github.jlmc.kafka4u.es.es.ElasticSearchIndexRepository;
import io.github.jlmc.kafka4u.es.es.RestHighLevelClientFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KafkaConsumerElasticsearchDispatcher implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerElasticsearchDispatcher.class);

    private final ElasticSearchIndexRepository elasticSearchIndexRepository;

    KafkaConsumerElasticsearchDispatcher(ElasticSearchIndexRepository elasticSearchIndexRepository) {
        this.elasticSearchIndexRepository = elasticSearchIndexRepository;
    }

    public static void main(String[] args) {

        KafkaConsumerElasticsearchDispatcher consumer =
                new KafkaConsumerElasticsearchDispatcher(
                        RestHighLevelClientFactory.createElasticSearchClient("localhost", "twitter_tweets"));


        Map<String, String> tuning = new HashMap<>();
        // the consumer id should id unique
        tuning.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        // Define the max pull records in the poll method,
        // this will hell in the balancing decreasing commit time
        // this a very common configuration in a very large company, market leaders
        // max.poll.records
        tuning.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

        // disable the default behavior of auto commit offsets
        // means that you have was developer have the responsibility to perform the commit of the offsets
        tuning.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaService<String> kafkaService =
                KafkaService.newKafkaService(
                        "localhost:9092",
                        "twitter_tweets",
                        KafkaConsumerElasticsearchDispatcher.class.getSimpleName(),
                        tuning);

        try (consumer; kafkaService) {
            kafkaService.addListener(consumer::consumeRecord);
            kafkaService.run();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaService::stopConsuming));

        System.out.println("Goodbye...");
    }


    public void consumeRecord(ConsumerRecord<String, String> record) {
        LOGGER.info("Consuming ## key <{}> partition <{}> offset <{}> timestamp <{}>, <{}>",
                record.key(), record.partition(), record.offset(), Instant.ofEpochMilli(record.timestamp()), record.value());

        //try {
        String saveId = elasticSearchIndexRepository.save(record.value());
        LOGGER.info("Save document In Elasticsearch with success, with the id <{}>", saveId);
        //} catch (Exception e) {
        //    LOGGER.error("Ignored error on persist data of the record, this way we are going to lose data!", e);
        //}
    }

    @Override
    public void close() {
        this.elasticSearchIndexRepository.close();
    }
}
