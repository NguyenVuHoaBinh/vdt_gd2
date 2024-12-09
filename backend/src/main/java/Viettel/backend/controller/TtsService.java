package Viettel.backend.controller;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class TtsService {

    private final String mlflowUrl = "http://localhost:5003/invocations"; // Your MLflow TTS model endpoint

    public byte[] generateSpeech(String text) throws RestClientException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Wrap the text in a JSON structure
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("inputs", text);  // Assuming MLflow expects a "text" field

        HttpEntity<Map<String, String>> ttsRequest = new HttpEntity<>(requestBody, headers);
        ResponseEntity<byte[]> ttsResponse = restTemplate.postForEntity(mlflowUrl, ttsRequest, byte[].class);

        if (ttsResponse.getStatusCode() == HttpStatus.OK) {
            return ttsResponse.getBody();
        } else {
            throw new RestClientException("Failed to generate speech: " + ttsResponse.getStatusCode());
        }
    }
}
