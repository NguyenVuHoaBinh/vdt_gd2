package Viettel.backend.service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class IndexService {

    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public IndexService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void createChatIndex() throws IOException {
        String indexName = "chat_index";

        if (!indexExists(indexName)) {
            CreateIndexRequest request = CreateIndexRequest.of(i -> i
                    .index(indexName)
                    .mappings(m -> m
                            .properties("sessionId", p -> p.keyword(k -> k))    // Adding sessionId as keyword for exact matches
                            .properties("userId", p -> p.keyword(k -> k))      // Adding userId as keyword for exact matches
                            .properties("timestamp", p -> p.date(d -> d))      // Adding timestamp as date type
                            .properties("message", p -> p.text(t -> t          // Adding message as text type for full-text search
                                    .analyzer("standard")))
                    )
            );

            CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(request);
            System.out.println("Index creation acknowledged: " + createIndexResponse.acknowledged());
        } else {
            System.out.println("Index '" + indexName + "' already exists.");
        }
    }

    private boolean indexExists(String indexName) {
        try {
            BooleanResponse existsResponse = elasticsearchClient.indices().exists(e -> e.index(indexName));
            return existsResponse.value();
        } catch (IOException e) {
            System.err.println("Error checking if index exists: " + e.getMessage());
            return false;
        }
    }

    public void createSchemaMetadataIndex(String indexName) throws IOException {
        // Check if the index already exists
        boolean indexExists = elasticsearchClient.indices().exists(e -> e.index(indexName)).value();
        if (indexExists) {
            System.out.println("Index '" + indexName + "' already exists.");
            return;
        }

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
                        .dims(1536) // Adjust according to your embedding dimension
                        .index(true)
                        .similarity("cosine")
                        .indexOptions(io -> io.type("int8_hnsw").m(16).efConstruction(100))
                )
        ));

        // Create the mapping
        TypeMapping mapping = new TypeMapping.Builder()
                .properties(properties)
                .build();

        // Define the index settings if needed
        IndexSettings settings = new IndexSettings.Builder()
                .build();

        // Create the index
        CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(c -> c
                .index(indexName)
                .mappings(mapping)
                .settings(settings)
        );

        if (createIndexResponse.acknowledged()) {
            System.out.println("Index '" + indexName + "' created successfully.");
        } else {
            System.out.println("Failed to create index '" + indexName + "'.");
        }
    }
}