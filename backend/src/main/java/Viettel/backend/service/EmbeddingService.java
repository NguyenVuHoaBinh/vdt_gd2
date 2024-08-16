package Viettel.backend.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmbeddingService {

    private final OpenAiEmbeddingModel embeddingModel;

    @Autowired
    public EmbeddingService(OpenAiApi openAiApi) {
        // Initialize the OpenAiEmbeddingModel with the provided OpenAiApi
        this.embeddingModel = new OpenAiEmbeddingModel(openAiApi);
    }

    // Method to return the EmbeddingModel
    public OpenAiEmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    public float[] generateEmbedding(String text) {
        // Create a map to hold any metadata, can be extended as needed
        Map<String, Object> metadata = new HashMap<>();

        // Create a Document object with content and metadata
        Document document = new Document(text, metadata);

        // Generate and return the embedding for the provided document
        return embeddingModel.embed(document);
    }
}
