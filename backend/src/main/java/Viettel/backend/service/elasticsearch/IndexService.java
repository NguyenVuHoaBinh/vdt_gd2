package Viettel.backend.service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);
    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public IndexService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void createChatIndex() throws IOException {
        String indexName = "chat_index";

        if (!indexExists(indexName)) {
            logger.info("Creating index: {}", indexName);
            try {
                CreateIndexRequest request = CreateIndexRequest.of(i -> i
                        .index(indexName)
                        .mappings(m -> m
                                .properties("sessionId", p -> p.keyword(k -> k))
                                .properties("userId", p -> p.keyword(k -> k))
                                .properties("timestamp", p -> p.date(d -> d))
                                .properties("message", p -> p.text(t -> t.analyzer("standard")))
                        )
                );

                CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(request);

                if (createIndexResponse.acknowledged()) {
                    logger.info("Index '{}' created successfully.", indexName);
                } else {
                    logger.warn("Index '{}' creation was not acknowledged.", indexName);
                }
            } catch (IOException e) {
                logger.error("Error occurred while creating index '{}': {}", indexName, e.getMessage(), e);
                throw e;
            }
        } else {
            logger.info("Index '{}' already exists.", indexName);
        }
    }

    private boolean indexExists(String indexName) {
        try {
            BooleanResponse existsResponse = elasticsearchClient.indices().exists(e -> e.index(indexName));
            return existsResponse.value();
        } catch (IOException e) {
            logger.error("Error checking if index '{}' exists: {}", indexName, e.getMessage(), e);
            return false;
        }
    }

    public void createSchemaMetadataIndex(String indexName) throws IOException {
        if (indexExists(indexName)) {
            logger.info("Index '{}' already exists.", indexName);
            return;
        }

        logger.info("Creating schema metadata index: {}", indexName);

        // Define the mapping for the index
        Map<String, Property> properties = new HashMap<>();
        properties.put("id", Property.of(p -> p.keyword(k -> k)));
        properties.put("chunk", Property.of(p -> p.keyword(k -> k)));
        properties.put("original_text", Property.of(p -> p.text(t -> t)));
        properties.put("chunk_index", Property.of(p -> p.integer(n -> n)));
        properties.put("parent_id", Property.of(p -> p.text(t -> t)));
        properties.put("chunk_token_count", Property.of(p -> p.keyword(k -> k)));
        properties.put("embedding", Property.of(p -> p
                .denseVector(dv -> dv
                        .dims(1536)
                        .index(true)
                        .similarity("cosine")
                        .indexOptions(io -> io.type("int8_hnsw").m(16).efConstruction(100))
                )
        ));

        try {
            // Create the mapping
            TypeMapping mapping = new TypeMapping.Builder().properties(properties).build();

            // Create the index
            CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(c -> c
                    .index(indexName)
                    .mappings(mapping)
            );

            if (createIndexResponse.acknowledged()) {
                logger.info("Index '{}' created successfully.", indexName);
            } else {
                logger.warn("Failed to create index '{}'.", indexName);
            }
        } catch (IOException e) {
            logger.error("Error occurred while creating schema metadata index '{}': {}", indexName, e.getMessage(), e);
            throw e;
        }
    }

    public void createSchemaElementsIndex(String indexName) throws IOException {
        if (indexExists(indexName)) {
            logger.info("Index '{}' already exists.", indexName);
            return;
        }

        logger.info("Creating schema elements index: {}", indexName);

        // Define the mapping for the schema elements index
        Map<String, Property> properties = new HashMap<>();
        properties.put("id", Property.of(p -> p.keyword(k -> k)));
        properties.put("content", Property.of(p -> p.text(t -> t.analyzer("standard"))));
        properties.put("type", Property.of(p -> p.keyword(k -> k)));
        properties.put("embedding", Property.of(p -> p
                .denseVector(dv -> dv
                        .dims(1536)
                        .index(true)
                        .similarity("cosine")
                        .indexOptions(io -> io.type("hnsw").m(16).efConstruction(100))
                )
        ));

        try {
            // Create the mapping
            TypeMapping mapping = new TypeMapping.Builder().properties(properties).build();

            // Create the index
            CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(c -> c
                    .index(indexName)
                    .mappings(mapping)
            );

            if (createIndexResponse.acknowledged()) {
                logger.info("Index '{}' created successfully.", indexName);
            } else {
                logger.warn("Failed to create index '{}'.", indexName);
            }
        } catch (IOException e) {
            logger.error("Error occurred while creating schema elements index '{}': {}", indexName, e.getMessage(), e);
            throw e;
        }
    }
}
