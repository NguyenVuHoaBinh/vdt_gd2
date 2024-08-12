package Viettel.backend.service;

import Viettel.backend.controller.ChatController;
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
public class LLMService {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;

    public LLMService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, String> processMessage(String message, String model, String role) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return callOpenAi(message, "gpt-3.5-turbo", role); // Use the appropriate model
            case "gemini":
                return callGemini(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    private Map<String, String> callOpenAi(String message, String model, String role) {
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 100);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                // Extract the SQL query from the full response
                String sqlQuery = openaiExtractSQLQuery(fullResponse);

                // Return both the full response and the extracted SQL query
                Map<String, String> result = new HashMap<>();
                result.put("fullResponse", fullResponse);
                result.put("sqlQuery", sqlQuery);
                return result;
            }

        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    private Map<String, String> callGemini(String message) {
        // Hypothetical API call to Gemini
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

                // Assuming a similar extraction logic for Gemini
                String sqlQuery = geminiExtractSQLQuery(fullResponse);

                // Return both the full response and the extracted SQL query
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

    private String openaiExtractSQLQuery(String fullResponse) {
        // Extract the SQL query using regular expressions or string processing
        String sqlQuery = "";

        // Assuming the query is enclosed in backticks or some identifiable pattern
        int startIndex = fullResponse.indexOf("```sql") + 6; // +6 to skip the "```sql" marker
        int endIndex = fullResponse.indexOf("```", startIndex);

        if (startIndex != -1 && endIndex != -1) {
            sqlQuery = fullResponse.substring(startIndex, endIndex).trim();
        }

        return sqlQuery;
    }

    private String geminiExtractSQLQuery(String fullResponse) {
        // Similar extraction logic for Gemini, if needed
        return openaiExtractSQLQuery(fullResponse);
    }
}
