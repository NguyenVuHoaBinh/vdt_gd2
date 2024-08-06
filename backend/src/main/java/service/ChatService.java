package service;

import model.ChatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${chatgpt.api.url}")
    private String chatgptApiUrl;

    @Value("${chatgpt.api.key}")
    private String chatgptApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(ChatRequest chatRequest) {
        if (chatRequest.getModel().equalsIgnoreCase("gemini")) {
            return callGeminiApi(chatRequest.getMessage());
        } else if (chatRequest.getModel().equalsIgnoreCase("chatgpt")) {
            return callChatGptApi(chatRequest.getMessage());
        }
        return "Invalid model specified.";
    }

    private String callGeminiApi(String message) {
        String url = geminiApiUrl + "/chat";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + geminiApiKey);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("message", message);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(url, requestEntity, String.class);
    }

    private String callChatGptApi(String message) {
        String url = chatgptApiUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + chatgptApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", message);
        requestBody.put("max_tokens", 50);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(url, requestEntity, String.class);
    }
}
