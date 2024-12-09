package Viettel.backend.controller;

import Viettel.backend.dto.ModelResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ModelController {

    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);

    @GetMapping("/models/search")
    public ResponseEntity<List<ModelResponse>> searchModelsByTag(@RequestParam("tagKey") String tagKey,
                                                                 @RequestParam("tagValue") String tagValue) {
        logger.info("Received request to search models with tagKey: {} and tagValue: {}", tagKey, tagValue);

        // Build the MLflow search URL with GET request
        String mlflowUrl = String.format("http://localhost:5001/api/2.0/mlflow/registered-models/search?filter=tags.%s = '%s'",
                tagKey, tagValue);
        logger.debug("Constructed MLflow URL: {}", mlflowUrl);

        // Create RestTemplate instance to make the GET request
        RestTemplate restTemplate = new RestTemplate();

        // Make the GET request to the MLflow API
        ResponseEntity<String> response;
        try {
            response = restTemplate.getForEntity(mlflowUrl, String.class);
            logger.info("Received response from MLflow API with status: {}", response.getStatusCode());
        } catch (Exception e) {
            logger.error("Failed to fetch data from MLflow API", e);
            return ResponseEntity.status(500).build();
        }

        // Parse the JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        List<ModelResponse> modelResponses = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode registeredModels = rootNode.get("registered_models");

            // Iterate over registered models and map to the DTO
            if (registeredModels != null) {
                logger.debug("Parsing registered models from response");
                for (JsonNode modelNode : registeredModels) {
                    ModelResponse modelResponse = new ModelResponse();
                    modelResponse.setName(modelNode.get("name").asText());

                    // Check if "latest_versions" exists and is an array before iterating
                    JsonNode latestVersionsNode = modelNode.get("latest_versions");
                    if (latestVersionsNode != null && latestVersionsNode.isArray()) {
                        List<String> modelVersions = new ArrayList<>();

                        // Iterate over the versions and add only the version number
                        for (JsonNode versionNode : latestVersionsNode) {
                            String version = versionNode.get("version").asText();
                            modelVersions.add(version);
                        }

                        // Set the versions in the model response
                        modelResponse.setVersions(modelVersions);
                    }

                    modelResponses.add(modelResponse);
                    logger.debug("Added model: {} with versions: {}", modelResponse.getName(), modelResponse.getVersions());
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing JSON response", e);
            return ResponseEntity.status(500).build();
        }

        logger.info("Returning {} models in response", modelResponses.size());
        return ResponseEntity.ok(modelResponses);
    }
}
