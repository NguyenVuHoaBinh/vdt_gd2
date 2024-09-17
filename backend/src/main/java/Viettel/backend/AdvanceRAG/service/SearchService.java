package Viettel.backend.AdvanceRAG.service;

import co.elastic.clients.json.JsonData;
import Viettel.backend.service.llmservice.EmbeddingService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final EmbeddingService embeddingService;

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

        // Convert queryEmbedding to List<Double> for script params
        List<Double> queryVector = new ArrayList<>();
        for (double f : queryEmbedding) {
            queryVector.add((double) f); // Convert float to double
        }

        // Create params map with JsonData
        Map<String, JsonData> params = new HashMap<>();
        params.put("queryVector", JsonData.of(queryVector));

        // Build the multi-match query
        Query textQuery = MultiMatchQuery.of(mm -> mm
                .fields(Arrays.asList("original_text", "keyphrases", "potential_questions", "entities"))
                .query(queryText)
        )._toQuery();

        // Build the script score query
        Script script = Script.of(s -> s
                .source("cosineSimilarity(params.queryVector, 'embedding') + 1.0")
                .params(params) // Use the new params map with JsonData
        );

        Query vectorQuery = ScriptScoreQuery.of(ss -> ss
                .query(MatchAllQuery.of(m -> m)._toQuery())
                .script(script)
        )._toQuery();

        // Build the bool query with should clauses
        BoolQuery boolQuery = BoolQuery.of(b -> b
                .should(textQuery)
                .should(vectorQuery)
        );

        Query finalQuery = boolQuery._toQuery();

        // Build the search request
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .size(numResults)
                .query(finalQuery)
        );

        // Execute the search
        SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

        // Extract hits
        List<Hit<Map>> hits = response.hits().hits();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Hit<Map> hit : hits) {
            results.add(hit.source());
        }

        return results;
    }
}
