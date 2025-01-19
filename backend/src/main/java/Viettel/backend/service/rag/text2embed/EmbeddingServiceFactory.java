package Viettel.backend.service.rag.text2embed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class EmbeddingServiceFactory {
    @Autowired
    private ApplicationContext context;

    private final Map<String, Function<String, EmbeddingService>>
            embeddings = new HashMap<>();

    public EmbeddingServiceFactory() {
        // TODO test with other models from other llm service
        // openai
        embeddings.put("text-embedding-ada-002", this::createOpenAiEmbeddingService);
        embeddings.put("text-embedding-3-large", this::createOpenAiEmbeddingService);
        embeddings.put("text-embedding-3-small", this::createOpenAiEmbeddingService);
    }

    public EmbeddingService createEmbeddingService(String model) {
        String MODEL = model.toLowerCase();
        Function<String, EmbeddingService> embeddingService = embeddings.get(MODEL);

        if (embeddingService == null) {
            MODEL = "text-embedding-ada-002";
            embeddingService = this::createOpenAiEmbeddingService;
        }

        return embeddingService.apply(MODEL);
    }

    // factories
    private EmbeddingService createOpenAiEmbeddingService(String model) {
        OpenAiEmbeddingService service = context.getBean(OpenAiEmbeddingService.class);
        service.setModel(model);
        return service;
    }
}
