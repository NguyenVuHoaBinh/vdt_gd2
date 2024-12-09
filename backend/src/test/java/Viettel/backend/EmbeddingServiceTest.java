package Viettel.backend;

import Viettel.backend.exception.EmbeddingServiceException;
import Viettel.backend.service.llmservice.EmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@PropertySource("classpath:application.properties")
class EmbeddingServiceTest {

    private EmbeddingService embeddingService;

    @Value("${openai.api.key}")
    private String apiKey;

    @BeforeEach
    void setUp() {
        // Initialize the real OpenAiApi with the actual API key
        OpenAiApi openAiApi = new OpenAiApi(apiKey);
        embeddingService = new EmbeddingService(openAiApi);
    }

    @Test
    void testGenerateEmbeddingSuccess() {
        String text = "Hello World";

        // This will make an actual API call to OpenAI and return the embedding
        float[] embedding = embeddingService.generateEmbedding(text);

        assertNotNull(embedding);
        assertTrue(embedding.length > 0);

        System.out.println("Generated embedding: ");
        for (float value : embedding) {
            System.out.print(value + " ");
        }
        System.out.println();
    }

    @Test
    void testGenerateEmbeddingFailure() {
        String invalidText = ""; // Assuming an empty string might trigger a failure

        // Expecting an exception if the text is invalid or the API fails
        assertThrows(EmbeddingServiceException.class, () -> embeddingService.generateEmbedding(invalidText));
    }
}
