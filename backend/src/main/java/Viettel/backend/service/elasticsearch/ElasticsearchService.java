package Viettel.backend.service.elasticsearch;

import Viettel.backend.model.DocumentWithEmbedding;
import Viettel.backend.model.MetadataDocument;
import Viettel.backend.service.llmservice.EmbeddingService;
import Viettel.backend.service.textextractor.TextExtractor;
import Viettel.backend.service.textextractor.TextExtractorFactory;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final EmbeddingService embeddingService;

    @Autowired
    public ElasticsearchService(ElasticsearchClient elasticsearchClient, EmbeddingService embeddingService) {
        this.elasticsearchClient = elasticsearchClient;
        this.embeddingService = embeddingService;
    }

    public String indexMetadataDocument(String indexName, String id, MetadataDocument document) throws IOException {
        // Create an IndexRequest to store the document in Elasticsearch
        IndexRequest<MetadataDocument> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(id)
                .document(document)
        );

        // Index the document in Elasticsearch
        IndexResponse response = elasticsearchClient.index(request);

        // Return the result of the indexing operation
        return response.result().name();
    }

    public <T> String indexChatDocument(String indexName, String sessionId, T document) throws IOException {
        // Extract text content from the document
        String textContent = extractTextFromDocument(document);

        // Generate an embedding for the document's text
        float[] embedding = embeddingService.generateEmbedding(textContent);

        // Generate a unique document ID
        String documentId = sessionId + "-" + UUID.randomUUID().toString();

        // Create an object that includes the document, embedding, and document ID
        DocumentWithEmbedding<T> docWithEmbedding = new DocumentWithEmbedding<>(documentId, document, embedding);

        // Create and execute the index request
        IndexRequest<DocumentWithEmbedding<T>> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(documentId)
                .document(docWithEmbedding)
        );

        IndexResponse response = elasticsearchClient.index(request);

        return response.result().name();
    }

    private <T> String extractTextFromDocument(T document) {
        TextExtractor<T> extractor = TextExtractorFactory.getExtractor(document);
        return extractor.extract(document);
    }


}
