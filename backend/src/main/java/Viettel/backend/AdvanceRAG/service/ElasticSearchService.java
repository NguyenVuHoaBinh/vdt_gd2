// src/main/java/Viettel/backend/service/elasticsearch/ElasticsearchService.java

package Viettel.backend.AdvanceRAG.service;

import Viettel.backend.service.llmservice.EmbeddingService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ElasticSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final EmbeddingService embeddingService;

    @Autowired
    public ElasticSearchService(ElasticsearchClient elasticsearchClient, EmbeddingService embeddingService) {
        this.elasticsearchClient = elasticsearchClient;
        this.embeddingService = embeddingService;
    }

    public String indexMetadataDocument(String indexName, String id, Map<String, Object> document) throws IOException {
        // Index the document
        IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(id)
                .document(document)
        );

        IndexResponse response = elasticsearchClient.index(request);

        return response.result().name();
    }


    // Existing methods...

    // Remove or adjust extractTextFromDocument if not needed
}
