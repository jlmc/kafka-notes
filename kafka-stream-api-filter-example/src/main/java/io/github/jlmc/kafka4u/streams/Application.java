package io.github.jlmc.kafka4u.streams;

import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

/**
 * Stream Filter Tweets
 *
 * <pre>
 *     kafka-topics --zookeeper zoo1:2181 --topic twitter_tweets --create --partitions 4 --replication-factor 1
 *     kafka-topics --zookeeper zoo1:2181 --topic important_tweets --create --partitions 3 --replication-factor 1
 * </pre>
 */
public class Application {

    private static final JsonParser jsonParser = new JsonParser();

    public static void main(String[] args) {
        //
        // Create properties
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        /* the consumer group using stream is called the application */
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "demo-kafka-streams");
        /* define the serializers */
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());


        //
        // Create a topology
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> inputTopic = streamsBuilder.stream("twitter_tweets");
        KStream<String, String> filteredStream = inputTopic.filter((k, jsonText) -> extractUserFollowersInTweet(jsonText) > 10_000);
        filteredStream.to("important_tweets");

        //
        // build the topology
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);

        //
        // start out stream application
        kafkaStreams.start();
    }

    private static int extractUserFollowersInTweet(String tweetJson) {
        try {
            return JsonParser.parseString(tweetJson)
                    .getAsJsonObject()
                    .get("user")
                    .getAsJsonObject()
                    .get("followers_count")
                    .getAsInt();

        } catch (NullPointerException e) {
            return 0;
        }
    }
}
