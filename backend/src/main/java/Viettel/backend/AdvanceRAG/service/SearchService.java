package Viettel.backend.AdvanceRAG.service;

import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.json.JsonData;
import Viettel.backend.service.llmservice.EmbeddingService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final EmbeddingService embeddingService;

    // @Autowired
    // private OpenAIEmbeddingService openAIEmbeddingService;

    public SearchService(ElasticsearchClient elasticsearchClient, EmbeddingService embeddingService) {
        this.elasticsearchClient = elasticsearchClient;
        this.embeddingService = embeddingService;
    }

    public List<Map<String, Object>> hybridSearch(
            String indexName,
            String queryText,
            double[] queryEmbedding,
            int numCandidates,
            int numResults) throws IOException {

        // dataForElasticsearch();

        // Validate input parameters
        if (queryEmbedding == null || queryEmbedding.length == 0) {
            throw new IllegalArgumentException("Query embedding must not be null or empty.");
        }

        if (numCandidates <= 0 || numResults <= 0) {
            throw new IllegalArgumentException("numCandidates and numResults must be positive integers.");
        }

        // Convert queryEmbedding to List<Double> for script params
        List<Double> queryVector = new ArrayList<>();
        for (double f : queryEmbedding) {
            queryVector.add(f); // Convert float to double
        }

        // Create params map with JsonData
        Map<String, JsonData> params = new HashMap<>();
        params.put("queryVector", JsonData.of(queryVector));

        // Build the text query targeting "originalText" or "original_text"
        Query textQuery = MultiMatchQuery.of(mm -> mm
                .fields(Arrays.asList("name"))
                .fields(Arrays.asList("authors_name"))
                .fields(Arrays.asList("label"))// Adjust based on which field you're using
                .query(queryText)
        )._toQuery();

        // Build the script score query for vector similarity
        Script script = Script.of(s -> s
                .source("cosineSimilarity(params.queryVector, 'vector_embedding') + 1.0")
                .params(params)

        );

        Query vectorQuery = ScriptScoreQuery.of(ss -> ss
                .query(MatchAllQuery.of(m -> m)._toQuery())
                .script(script)
                .boost(3F)
        )._toQuery();

        // Combine the text and vector queries using a bool query
        BoolQuery boolQuery = BoolQuery.of(b -> b
                .should(textQuery)    // Include the text query
                .should(vectorQuery)  // Include the vector query
                .minimumShouldMatch("1") // Ensure at least one clause matches
        );

        Query finalQuery = boolQuery._toQuery();

        // Build the search request
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .size(numCandidates) // Retrieve more candidates initially
                .query(finalQuery)
        );

        List<Map<String, Object>> results = new ArrayList<>();

        try {
            // Execute the search
            SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

            // Extract hits
            List<Hit<Map>> hits = response.hits().hits();

            for (Hit<Map> hit : hits) {
                results.add(hit.source());
                if (results.size() >= numResults) {
                    break; // Stop once we have the desired number of results
                }
            }
        } catch (IOException e) {
            System.err.println("Error executing Elasticsearch search: " + e.getMessage());
            throw e;
        }

        return results;
    }

//    public void dataForElasticsearch() {
//        List<Map<String, Object>> books = List.of(
//                Map.of(
//                        "id", "1",
//                        "name", "The Great Gatsby",
//                        "authors_name", "F. Scott Fitzgerald",
//                        "label", "Classic Literature",
//                        "view", 1234
//                ),
//                Map.of(
//                        "id", "2",
//                        "name", "To Kill a Mockingbird",
//                        "authors_name", "Harper Lee",
//                        "label", "Historical Fiction",
//                        "view", 5678
//                )
//        );
//
//        books.forEach(book -> {
//            Map<String, Object> modifiableBook = new HashMap<>(book);  // Ensure map is mutable
//            String text = modifiableBook.get("name") + " " + modifiableBook.get("authors_name") + " " + modifiableBook.get("label");
//            double[] embedding = openAIEmbeddingService.getEmbedding(text);
//            modifiableBook.put("vector_embedding", embedding);
//
//            // Index into Elasticsearch
//            IndexRequest<Map<String, Object>> request = new IndexRequest.Builder<Map<String, Object>>()
//                    .index("books")
//                    .id(modifiableBook.get("id").toString())
//                    .document(modifiableBook)
//                    .build();
//
//            try {
//                elasticsearchClient.index(request);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }
}
