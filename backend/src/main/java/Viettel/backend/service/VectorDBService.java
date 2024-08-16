package Viettel.backend.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VectorDBService {

    private final RedisVectorStore redisVectorStore;
    private final EmbeddingService embeddingService;

    @Autowired
    public VectorDBService(RedisVectorStore redisVectorStore, EmbeddingService embeddingService) {
        this.redisVectorStore = redisVectorStore;
        this.embeddingService = embeddingService;
    }

    public String semanticSearch(String userQuery, String metadata) {
        // Combine the user query and metadata
        String combinedQuery = userQuery + " " + metadata;

        // Perform semantic search in Redis
        List<Document> searchResults = redisVectorStore.similaritySearch(
                SearchRequest.query(combinedQuery)
                        .withTopK(5)  // Fetch top 5 results
                        .withSimilarityThreshold(0.5)  // Set a similarity threshold
        );

        // Extract and return content from search results
        return searchResults.isEmpty() ? "No relevant documents found." : searchResults.stream()
                .map(Document::getContent)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("No relevant documents found.");
    }

    public void storeChatAndMetadata(String sessionId, String description, Map<String, String> metadata) {
        // Convert metadata to a string format for embedding
        StringBuilder metadataString = new StringBuilder();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            metadataString.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        // Generate embedding
        float[] embedding = embeddingService.generateEmbedding(metadataString.toString());

        // Prepare metadata for storage
        Map<String, Object> documentMetadata = new HashMap<>();
        documentMetadata.put("sessionId", sessionId);
        documentMetadata.put("description", description);
        documentMetadata.putAll(metadata);

        // Create a Document object and store it in the Redis vector store
        Document document = new Document(metadataString.toString(), documentMetadata);
        document.setEmbedding(embedding);

        redisVectorStore.add(List.of(document));
    }

    public void storeUserChat(String sessionId, String userMessage, String modelResponse) {
        // Prepare metadata for the chat
        Map<String, Object> chatMetadata = new HashMap<>();
        chatMetadata.put("sessionId", sessionId);
        chatMetadata.put("userMessage", userMessage);

        // Generate embedding for the chat content
        float[] embedding = embeddingService.generateEmbedding(userMessage + "\n" + modelResponse);

        // Create a Document object and store it in the Redis vector store
        Document document = new Document(modelResponse, chatMetadata);
        document.setEmbedding(embedding);

        redisVectorStore.add(List.of(document));
    }
}