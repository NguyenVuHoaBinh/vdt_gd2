package Viettel.backend.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class TextSpeechController {

    private static final String MLFLOW_URL = "http://127.0.0.1:5003/invocations";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Constructor injection for RestTemplate and ObjectMapper
    public TextSpeechController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends text input to the MLflow model and returns a Base64-encoded audio response.
     *
     * @param text The input text to be converted to speech.
     * @return A Base64-encoded string representing the audio output.
     */
    public static String sendTextToMlflow(String text) {
        try {
            // Set up the HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);


            // Prepare the JSON payload
            Map<String, Object> inputRecord = new HashMap<>();
            inputRecord.put("inputs", text);
            Map<String, Object> payload = new HashMap<>();
            payload.put("dataframe_records", Collections.singletonList(inputRecord));

            // Create HTTP entity with payload and headers
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            // Send the request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(MLFLOW_URL, requestEntity, String.class);

            // Parse the response JSON to extract the Base64-encoded audio
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseJson = objectMapper.readTree(response.getBody());

                // Access the first item in "predictions" array, then get "outputs"
                JsonNode predictions = responseJson.get("predictions");
                if (predictions != null && predictions.isArray() && predictions.size() > 0) {
                    JsonNode firstPrediction = predictions.get(0);
                    JsonNode outputs = firstPrediction.get("outputs");
                    if (outputs != null) {
                        return outputs.asText();
                    } else {
                        System.err.println("No 'outputs' field found in the prediction.");
                    }
                } else {
                    System.err.println("No 'predictions' array found or it is empty.");
                }
            } else {
                System.err.println("Error response from MLflow: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}