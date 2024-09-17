package Viettel.backend.service.llmservice;

import Viettel.backend.exception.EmbeddingServiceException;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for generating text embeddings using the OpenAI API.
 */
@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);

    private final OpenAiEmbeddingModel embeddingModel;

    OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
            .withModel("text-embedding-ada-002")  // Using OpenAI's Ada embedding model
            .build();

    /**
     * Constructor for EmbeddingService.
     *
     * @param openAiApi The OpenAI API instance used for embedding generation.
     */
    public EmbeddingService(OpenAiApi openAiApi) {
        this.embeddingModel = new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }

    /**
     * Generates an embedding for the given text.
     *
     * @param text The text to generate an embedding for.
     * @return A float array representing the embedding of the text.
     * @throws EmbeddingServiceException If embedding generation fails.
     */
    public float[] generateEmbedding(String text) {
        Map<String, Object> metadata = new HashMap<>();
        Document document = new Document(text, metadata);

        try {
            float[] embedding = embeddingModel.embed(document);
            logger.info("Successfully generated embedding for text: {}", text);
            return embedding;
        } catch (Exception e) {
            logger.error("Failed to generate embedding for text: {}", text, e);
            throw new EmbeddingServiceException("Failed to generate embedding", e);
        }
    }

    /**
     * Generates embeddings for a list of texts (batch processing).
     *
     * @param texts A list of strings to generate embeddings for.
     * @return A list of float arrays, where each array represents the embedding of one text.
     * @throws EmbeddingServiceException If embedding generation fails.
     */
    public List<float[]> generateEmbeddingsForBatch(List<String> texts) {
        List<float[]> embeddings = new ArrayList<>();

        try {
            for (String text : texts) {
                float[] embedding = generateEmbedding(text);
                embeddings.add(embedding);
            }
            logger.info("Successfully generated embeddings for batch of texts.");
        } catch (Exception e) {
            logger.error("Failed to generate embeddings for batch of texts.", e);
            throw new EmbeddingServiceException("Failed to generate embeddings for batch", e);
        }

        return embeddings;
    }

    /**
     * Embeds the text fields of the given list of document objects and updates them with embeddings.
     *
     * @param documents A list of document objects, each containing text to embed.
     * @param textField The field name to extract text from (e.g., "chunk").
     * @param embeddingFieldPostfix The field name postfix where the embedding should be stored (e.g., "_embedding").
     * @param batchSize The number of documents to process in each batch.
     * @throws EmbeddingServiceException If embedding generation fails.
     */
    public void embedDocumentsTextWise(List<Map<String, Object>> documents,
                                       String textField,
                                       String embeddingFieldPostfix,
                                       int batchSize) {
        try {
            // Process documents in batches
            for (int i = 0; i < documents.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, documents.size());
                List<Map<String, Object>> batch = documents.subList(i, endIndex);

                // Extract texts from the batch of documents
                List<String> texts = new ArrayList<>();
                for (Map<String, Object> doc : batch) {
                    texts.add((String) doc.get(textField));
                }

                // Generate embeddings for the batch
                List<float[]> embeddings = generateEmbeddingsForBatch(texts);

                // Update the documents with their respective embeddings
                for (int j = 0; j < batch.size(); j++) {
                    batch.get(j).put(textField + embeddingFieldPostfix, embeddings.get(j));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to embed documents in batch.", e);
            throw new EmbeddingServiceException("Failed to embed documents", e);
        }
    }
}
