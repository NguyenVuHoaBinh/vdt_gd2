package Viettel.backend.AdvanceRAG.model;

import Viettel.backend.AdvanceRAG.service.OpenAIEmbeddingService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EmbeddingModel {

    private final OpenAIEmbeddingService embeddingService;

    public EmbeddingModel(OpenAIEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public List<double[]> getEmbeddingsFromText(List<String> texts) {
        return embeddingService.getEmbeddings(texts);
    }

    // Generate embedding for a single text
    public double[] generateEmbedding(String text) {
        return embeddingService.getEmbedding(text);
    }

    public String embedDocumentsTextWise(List<Map<String, Object>> documents, String textField, String embeddingFieldPostfix, int batchSize) {
        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<Map<String, Object>> batch = documents.subList(i, end);
            List<String> texts = new ArrayList<>();
            for (Map<String, Object> doc : batch) {
                texts.add((String) doc.get(textField));
            }

            List<double[]> embeddings = getEmbeddingsFromText(texts);

            for (int j = 0; j < batch.size(); j++) {
                batch.get(j).put(textField + embeddingFieldPostfix, embeddings.get(j));
            }
        }
        return textField + embeddingFieldPostfix;
    }
}