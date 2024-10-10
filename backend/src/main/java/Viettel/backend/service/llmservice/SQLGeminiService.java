package Viettel.backend.service.llmservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SQLGeminiService implements LLMServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(SQLGeminiService.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;

    public SQLGeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<String, String> processMessage(String message, String role) {
        String url = "https://api.gemini.com/v1/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(geminiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", message);
        requestBody.put("max_tokens", 100);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                String fullResponse = (String) choices.get(0).get("text");

                String sqlQuery = extractSQLQuery(fullResponse);

                Map<String, String> result = new HashMap<>();
                result.put("fullResponse", fullResponse);
                result.put("sqlQuery", sqlQuery);
                return result;
            }
        } else {
            throw new RuntimeException("Failed to get response from Gemini");
        }
        return null;
    }

    @Override
    public String processAnalysis(String message) {
        return "";
    }

    @Override
    public String generateHyDE(String message) {
        return "";
    }

    @Override
    public String generateRefinedQuery(String message) {
        return "";
    }

    @Override
    public String errorSolver(String message) {
        return "";
    }

    private String extractSQLQuery(String fullResponse) {
        // Similar extraction logic as OpenAiService
        return fullResponse;
    }
}
