package Viettel.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SpeechTextController {

    private static final Logger logger = LoggerFactory.getLogger(SpeechTextController.class);

    @PostMapping("/process-audio")
    public ResponseEntity<?> processAudio(@RequestParam("file") MultipartFile file) {
        logger.info("Received request to process audio");

        if (file.isEmpty()) {
            logger.warn("No file was selected for upload");
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No selected file"));
        }

        File tempFile = null;
        try {
            // Save the uploaded file to a temporary file
            tempFile = File.createTempFile("audio-", ".wav");
            file.transferTo(tempFile);
            String tempFilePath = tempFile.getAbsolutePath();
            logger.debug("Temporary file created at: {}", tempFilePath);

            // Prepare the payload for the MLflow model
            Map<String, Object> payload = new HashMap<>();
            payload.put("inputs", Collections.singletonList(tempFilePath));
            logger.debug("Payload prepared for MLflow: {}", payload);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the HTTP entity with the payload and headers
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            // Send the request to the MLflow model serving endpoint
            RestTemplate restTemplate = new RestTemplate();
            String mlflowUrl = "http://127.0.0.1:5002/invocations";  // Adjust the URL and port if needed
            logger.debug("Sending request to MLflow URL: {}", mlflowUrl);

            ResponseEntity<String> mlflowResponse = restTemplate.postForEntity(
                    mlflowUrl, requestEntity, String.class);

            if (mlflowResponse.getStatusCode().is2xxSuccessful()) {
                // Parse the response from the MLflow model
                String responseBody = mlflowResponse.getBody();
                logger.info("Received successful response from MLflow model");

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseJson = objectMapper.readTree(responseBody);
                String transcription = responseJson.get("predictions").get(0).asText();

                // Return the transcription to the client
                logger.info("Returning transcription to client");
                return ResponseEntity.ok(Collections.singletonMap("transcription", transcription));
            } else {
                // Handle errors from the MLflow model
                logger.error("Error response from MLflow model: {}", mlflowResponse.getBody());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Error from MLflow model: " + mlflowResponse.getBody()));
            }
        } catch (Exception e) {
            logger.error("Error processing audio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error processing audio"));
        } finally {
            // Clean up the temporary file
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (deleted) {
                    logger.debug("Temporary file deleted successfully: {}", tempFile.getAbsolutePath());
                } else {
                    logger.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
}
