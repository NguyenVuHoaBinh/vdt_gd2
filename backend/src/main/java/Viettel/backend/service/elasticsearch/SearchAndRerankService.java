package Viettel.backend.service.elasticsearch;

import Viettel.backend.model.DocumentWithEmbedding;
import co.elastic.clients.json.JsonData;
import Viettel.backend.service.llmservice.EmbeddingService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchAndRerankService {
    //TODO: Implement reranking technique
    private final ElasticsearchClient elasticsearchClient;
    private final EmbeddingService embeddingService;
    private final PreRetrievalService preRetrievalService;

    @Autowired
    public SearchAndRerankService(ElasticsearchClient elasticsearchClient, EmbeddingService embeddingService, PreRetrievalService preRetrievalService) {
        this.elasticsearchClient = elasticsearchClient;
        this.embeddingService = embeddingService;
        this.preRetrievalService = preRetrievalService;
    }

    public List<DocumentWithEmbedding> searchDocuments(String indexName, String query, boolean rerank) throws IOException {
        // Pre-retrieval: Expand the query
        String expandedQuery = preRetrievalService.expandQuery(query);

        // Perform the search
        List<DocumentWithEmbedding> results = searchDocumentsWithQuery(indexName, expandedQuery);

        // If reranking is enabled, rerank the results
        if (rerank) {
            return rerankResults(results);
        }

        // Return the results as-is if reranking is not enabled
        return results;
    }

    private List<DocumentWithEmbedding> searchDocumentsWithQuery(String indexName, String query) throws IOException {
        // Generate the query embedding
        float[] queryEmbedding = embeddingService.generateEmbedding(query);

        // Prepare parameters map and convert the embedding to JsonData
        Map<String, JsonData> params = new HashMap<>();
        params.put("query_embedding", JsonData.of(queryEmbedding));

        // Perform the search
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .size(1)
                .query(q -> q
                        .scriptScore(ss -> ss
                                .query(matchAll -> matchAll.matchAll(m -> m))
                                .script(script -> script
                                        .source("cosineSimilarity(params.query_embedding, 'embedding') + 1.0")
                                        .params(params)
                                )
                        )
                )
        );

        SearchResponse<DocumentWithEmbedding> searchResponse = elasticsearchClient.search(searchRequest, DocumentWithEmbedding.class);

        // Extract the results
        return searchResponse.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    // Placeholder for reranking, can be implemented later
    private List<DocumentWithEmbedding> rerankResults(List<DocumentWithEmbedding> results) {
        // Custom reranking logic would go here
        // For now, it returns the results unchanged
        return results;
    }

    public String searchRelatedMetadata(String indexName, String databaseName, String query) throws IOException {
        String combinedQuery = databaseName + " " + query;
        List<?> results = searchDocumentsWithQuery(indexName, combinedQuery);

        List<DocumentWithEmbedding<Map<String, Object>>> metadataResults = (List<DocumentWithEmbedding<Map<String, Object>>>) results;

        if (metadataResults != null && !metadataResults.isEmpty()) {
            return metadataResults.stream()
                    .map(result -> (String) result.getDocument().get("schema")) // Extract the "schema" field
                    .collect(Collectors.joining("\n"));
        } else {
            return "";
        }
    }



}
