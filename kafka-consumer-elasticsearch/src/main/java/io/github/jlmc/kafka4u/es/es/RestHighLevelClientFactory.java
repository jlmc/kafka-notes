package io.github.jlmc.kafka4u.es.es;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public final class RestHighLevelClientFactory {

    public static ElasticSearchIndexRepository createElasticSearchClient(String host, String index) {
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(host, 9200, "http"),
                        new HttpHost(host, 9201, "http")));

        return new ElasticSearchIndexRepository("twitter_tweets", restHighLevelClient);
    }
}
