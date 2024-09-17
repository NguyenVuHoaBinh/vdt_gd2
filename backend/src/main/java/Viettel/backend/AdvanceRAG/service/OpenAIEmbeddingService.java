package Viettel.backend.AdvanceRAG.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class OpenAIEmbeddingService {

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.embedding.model}")
    private String embeddingModel;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/embeddings";

    public OpenAIEmbeddingService() {
        this.restTemplate = new RestTemplate();
    }

    public List<double[]> getEmbeddings(List<String> texts) {
        // Prepare the request payload
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", texts);
        requestBody.put("model", embeddingModel);

        // Set up the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Make the POST request
        ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Handle the response
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");

            List<double[]> embeddings = new ArrayList<>();
            for (Map<String, Object> item : data) {
                List<Double> embeddingList = (List<Double>) item.get("embedding");
                double[] embeddingArray = embeddingList.stream().mapToDouble(Double::doubleValue).toArray();
                embeddings.add(embeddingArray);
            }
            return embeddings;
        } else {
            // Handle errors appropriately
            throw new RuntimeException("Failed to get embeddings from OpenAI API. Status code: " + response.getStatusCode());
        }
    }

    public double[] getEmbedding(String text) {
        // Prepare the request payload
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", text);
        requestBody.put("model", embeddingModel);

        // Set up the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Make the POST request
        ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Handle the response
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");

            // First, calculate the total size needed for the final float[] array
            int totalSize = 0;
            for (Map<String, Object> item : data) {
                List<Double> embeddingList = (List<Double>) item.get("embedding");
                totalSize += embeddingList.size();
            }

            // Create a single double[] array to hold all embeddings
            double[] embeddings = new double[totalSize];

            // Copy each embedding's data into the final array
            int index = 0;
            for (Map<String, Object> item : data) {
                List<Double> embeddingList = (List<Double>) item.get("embedding");
                for (Double value : embeddingList) {
                    embeddings[index++] = value.floatValue(); // Convert to float and add to final array
                }
            }

            return embeddings;

    } else {
            // Handle errors appropriately
            throw new RuntimeException("Failed to get embeddings from OpenAI API. Status code: " + response.getStatusCode());
        }
    }
}

