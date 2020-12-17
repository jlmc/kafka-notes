package io.github.jlmc.kafka4.beginners.realworld;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import io.github.jlmc.kafka4.beginners.producers.KafkaDispatcher;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    public static final String TWITTER_TWEETS = "twitter_tweets";
    private final TwitterApiCredentials apiCredentials;

    TwitterProducer(TwitterApiCredentials apiCredentials) {
        this.apiCredentials = apiCredentials;
    }

    public static void main(String[] args) {

        TwitterApiCredentials apiCredentials =
                new TwitterApiCredentials("1z6RTU5M0DRsVAz0vhCoyyp3N",
                        "pHxb6BuW7bCtUZ0kbUWtQmjly1I6JvSkdEwyCZdBbniYYwn431",
                        "4372780893-mm0saAAk3VIwYIFLGdqQXKbxp6pzGgSJBYFnpat",
                        "aOlZKlxVG5QOwKQOYnDAtuSoXmntMoxyI6JWebORoFbur");
        new TwitterProducer(apiCredentials).run();
    }

    public void run() {
        /* Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(100000);

        // create a twitter client
        Client twitterClient = createTwitterClient(msgQueue);
        twitterClient.connect();

        // create a kafka producer
        dispatchToKafkaTopic(msgQueue, twitterClient);
    }

    private void dispatchToKafkaTopic(BlockingQueue<String> msgQueue, Client twitterClient) {
        Map<String, String> tuningConfigs = new HashMap<>();

        // create a safe producer
        tuningConfigs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        tuningConfigs.put(ProducerConfig.ACKS_CONFIG, "all");
        // not required, just for the people not get confused,
        tuningConfigs.put(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        tuningConfigs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        // high throughput producer (at expense at a bit of latency and cpu usage
        tuningConfigs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        tuningConfigs.put(ProducerConfig.LINGER_MS_CONFIG, "20");
        tuningConfigs.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024));  // 32 kb of batch size


        try (KafkaDispatcher<String> kafkaDispatcher = KafkaDispatcher.newKafkaDispatcher(tuningConfigs)) {
            // on a different thread, or multiple different threads....
            while (!twitterClient.isDone()) {
                try {
                    String msg = msgQueue.poll(5, TimeUnit.SECONDS);

                    if (msg == null) {
                        continue;
                    }

                    // kafka-topics --zookeeper zoo1:2181 --create --topic twitter_tweets --partitions 4 --replication-factor 1
                    // kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic twitter_tweets
                    kafkaDispatcher.send(TWITTER_TWEETS, null, msg);

                } catch (InterruptedException e) {
                    twitterClient.stop();
                }
            }
        }
    }

    private Client createTwitterClient(BlockingQueue<String> msgQueue) {
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

        //List<String> terms = Lists.newArrayList("twitter", "api");
        List<String> terms = List.of("kafka", "java", "usa", "benfica");
        hosebirdEndpoint.trackTerms(terms);

        //@formatter:off
        // These secrets should be read from a config file
        Authentication hosebirdAuth =
                new OAuth1(apiCredentials.consumerKey(),
                           apiCredentials.consumerSecret(),
                           apiCredentials.token(),
                           apiCredentials.secret());
        //@formatter:on

        ClientBuilder builder =
                new ClientBuilder()
                        .name("Hosebird-Client-01")                              // optional: mainly for the logs
                        .hosts(hosebirdHosts)
                        .authentication(hosebirdAuth)
                        .endpoint(hosebirdEndpoint)
                        .processor(new StringDelimitedProcessor(msgQueue));

        return builder.build();
    }

    public static record TwitterApiCredentials(String consumerKey,
                                               String consumerSecret,
                                               String token,
                                               String secret) {
    }
}
