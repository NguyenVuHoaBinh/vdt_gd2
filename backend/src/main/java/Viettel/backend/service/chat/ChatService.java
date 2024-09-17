package Viettel.backend.service.chat;

import Viettel.backend.model.ChatDocument;
import Viettel.backend.service.llmservice.EmbeddingService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
public class ChatService {

    private final ElasticsearchClient elasticsearchClient;
    private final EmbeddingService embeddingService;

    @Autowired
    public ChatService(ElasticsearchClient elasticsearchClient, EmbeddingService embeddingService) {
        this.elasticsearchClient = elasticsearchClient;
        this.embeddingService = embeddingService;
    }

    public String indexChat(String userId, String message) throws IOException {
        // Generate an embedding for the message
        float[] embedding = embeddingService.generateEmbedding(message);

        // Create a ChatDocument
        ChatDocument chatDocument = new ChatDocument(userId, new Date(), message);

        // Index the document in Elasticsearch
        IndexRequest<ChatDocument> request = IndexRequest.of(i -> i
                .index("chat_index")
                .document(chatDocument)
        );

        IndexResponse response = elasticsearchClient.index(request);

        return response.result().name();
    }
}
