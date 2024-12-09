package Viettel.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class viAnController {
    private static final String VIAN_URL = "http://171.224.244.159:8106/textToSpeechApi/tts";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Constructor injection for RestTemplate and ObjectMapper
    public viAnController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String sendTextVIAN(String text) {
        try {
            // Set up the HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth("dGVzdDoxcWF6WFNXQDNlZEM=");

            // Prepare the payload for the MLflow model
            Map<String, Object> payload = new HashMap<>();
            payload.put("scripts", Collections.singletonList(text));

            // Create HTTP entity with payload and headers
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            // Send the request
            ResponseEntity<String> response = restTemplate.postForEntity(VIAN_URL, requestEntity, String.class);

            // Log response status and body
            //System.out.println("Response status: " + response.getStatusCode());
            //System.out.println("Response body: " + response.getBody());

            // Parse the response JSON to extract the Base64-encoded audio
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());

                // Extract the "base64" field, which is an array, and get the first element
                JsonNode base64Array = responseJson.get("base64");
                if (base64Array != null && base64Array.isArray() && base64Array.size() > 0) {
                    JsonNode base64Node = base64Array.get(0);
                    if (base64Node != null && !base64Node.asText().isEmpty()) {
                        return base64Node.asText();
                    }
                } else {
                    System.err.println("Error: 'base64' field is missing or empty in the response: " + response.getBody());
                }
            } else {
                System.err.println("Received non-success status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
