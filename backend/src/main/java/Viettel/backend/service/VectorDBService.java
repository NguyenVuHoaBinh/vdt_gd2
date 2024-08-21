package Viettel.backend.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VectorDBService {

    private final RedisVectorStore redisVectorStore;
    private final EmbeddingService embeddingService; // Ensure the EmbeddingService is injected

    @Autowired
    public VectorDBService(RedisVectorStore redisVectorStore, EmbeddingService embeddingService) {
        this.redisVectorStore = redisVectorStore;
        this.embeddingService = embeddingService;
    }

    // Store session metadata embedding
    public void storeSessionMetadata(String sessionId, String description, Map<String, String> metadata) {
        StringBuilder metadataString = new StringBuilder();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            metadataString.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        float[] embedding = embeddingService.generateEmbedding(metadataString.toString());

        Map<String, Object> documentMetadata = new HashMap<>();
        documentMetadata.put("sessionId", sessionId);
        documentMetadata.put("description", description);
        documentMetadata.putAll(metadata);

        Document document = new Document(metadataString.toString(), documentMetadata);
        document.setEmbedding(embedding);

        // Wrap the Document in a List before adding it to the RedisVectorStore
        redisVectorStore.add(Collections.singletonList(document));
    }

    // Store chat message embedding
    public void storeUserChat(String sessionId, String messageId, String userMessage, String modelResponse) {
        String combinedContent = userMessage + "\n" + modelResponse;
        float[] embedding = embeddingService.generateEmbedding(combinedContent);

        Map<String, Object> chatMetadata = new HashMap<>();
        chatMetadata.put("sessionId", sessionId);
        chatMetadata.put("messageId", messageId);
        chatMetadata.put("userMessage", userMessage);

        Document document = new Document(combinedContent, chatMetadata);
        document.setEmbedding(embedding);

        // Wrap the Document in a List before adding it to the RedisVectorStore
        redisVectorStore.add(Collections.singletonList(document));
    }

    // Perform semantic search for session metadata
    public String searchSessionMetadata(String sessionId, String userQuery) {
        return performSemanticSearch(userQuery);
    }

    // Perform semantic search for chat messages
    public String searchChatMessages(String sessionId, String userQuery) {
        return performSemanticSearch(userQuery);
    }

    // Helper method for performing semantic search
    private String performSemanticSearch(String userQuery) {
        List<Document> searchResults = redisVectorStore.similaritySearch(
                SearchRequest.query(userQuery)
                        .withTopK(5)
                        .withSimilarityThreshold(0.5)
        );

        return searchResults.isEmpty() ? "No relevant documents found."
                : searchResults.stream()
                .map(Document::getContent)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("No relevant documents found.");
    }
}
