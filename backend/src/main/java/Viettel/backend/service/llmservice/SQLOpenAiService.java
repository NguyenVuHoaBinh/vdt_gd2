package Viettel.backend.service.llmservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SQLOpenAiService implements LLMServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(SQLOpenAiService.class);

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate;

    public SQLOpenAiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<String, String> processMessage(String message, String role) {
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

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

    @Override
    public String processAnalysis(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/SQLSystemPrompt.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String generateHyDE(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/generateHYDE.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String generateRefinedQuery(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/refiningQuery.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    private String extractSQLQuery(String fullResponse) {
        String sqlQuery = "";
        int startIndex = fullResponse.indexOf("```sql") + 6;
        int endIndex = fullResponse.indexOf("```", startIndex);

        if (startIndex != -1 && endIndex != -1) {
            sqlQuery = fullResponse.substring(startIndex, endIndex).trim();
        }

        return sqlQuery;
    }
}