package io.github.jlmc.kafka4u.es.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

public class ElasticSearchIndexRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchIndexRepository.class);

    private final String index;
    private final RestHighLevelClient esClient;
    private final ObjectMapper objectMapper;

    ElasticSearchIndexRepository(String index, RestHighLevelClient restHighLevelClient) {
        this.index = index;
        this.esClient = restHighLevelClient;
        objectMapper = new ObjectMapper();
    }

    public void saveInBulk(Collection<String> jsonTexts) {
        Objects.requireNonNull(jsonTexts);
        if (jsonTexts.isEmpty()) {
            return;
        }
        try {

            BulkRequest bulkRequest = new BulkRequest();

            for (String jsonText : jsonTexts) {
                IndexRequest indexRequest = toIndexrequest(jsonText);
                bulkRequest.add(indexRequest);
            }

            BulkResponse response = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);

            if (response.status() != RestStatus.OK) {
                throw new IllegalStateException("No all the request have been correctlly handler by the elasticsearch server");
            }

        } catch (IOException e) {
            throw handlerIOException(e);
        }
    }

    private UncheckedIOException handlerIOException(IOException e) {
        LOGGER.error("Unexpected error happens in the communication with the ES host index", e);
        return new UncheckedIOException(e);
    }

    public ElasticsearchDocument save(String jsonText) {
        try {

            IndexRequest indexRequest = toIndexrequest(jsonText);

            IndexResponse indexResponse = esClient.index(indexRequest, RequestOptions.DEFAULT);

            return new ElasticsearchDocument(indexResponse.getId(), jsonText);

        } catch (IOException e) {
            throw handlerIOException(e);
        }
    }

    private IndexRequest toIndexrequest(String jsonText) {
        String id =
                extractIdFromJson(jsonText, "id_str")
                        .orElseThrow(() -> new IllegalArgumentException("No Id found in the json text"));

        return new IndexRequest(index)
                .source(jsonText, XContentType.JSON)
                .id(id);
    }

    public void close() {
        try {
            this.esClient.close();
        } catch (IOException e) {
            LOGGER.error("Unexpected error happens in the closing ES client", e);
            throw new UncheckedIOException(e);
        }
    }

    public Optional<String> extractIdFromJson(String jsonText, String propertyName) {
        try {
            ObjectNode node = objectMapper.readValue(jsonText, ObjectNode.class);

            if (!node.has(propertyName)) {
                throw new NoSuchElementException("No Such element <'" + propertyName + "'>");
            }

            JsonNode jsonNode = node.get(propertyName);

            if (jsonNode.isNull()) {
                return Optional.empty();
            }

            return Optional.of(jsonNode.asText());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid Json Text to process", e);
        }
    }
}
