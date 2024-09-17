package Viettel.backend.controller;

import Viettel.backend.dto.ModelResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ModelController {

    @GetMapping("/models/search")
    public ResponseEntity<List<ModelResponse>> searchModelsByTag(@RequestParam("tagKey") String tagKey,
                                                    @RequestParam("tagValue") String tagValue) {
        // Build the MLflow search URL with GET request
        String mlflowUrl = String.format("http://localhost:5001/api/2.0/mlflow/registered-models/search?filter=tags.%s = '%s'",
                tagKey, tagValue);

        // Create RestTemplate instance to make the GET request
        RestTemplate restTemplate = new RestTemplate();

        // Make the GET request to the MLflow API
        ResponseEntity<String> response = restTemplate.getForEntity(mlflowUrl, String.class);

        // Parse the JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        List<ModelResponse> modelResponses = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode registeredModels = rootNode.get("registered_models");

            // Iterate over registered models and map to the DTO
            if (registeredModels != null) {
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return the list of models as a JSON response
        return ResponseEntity.ok(modelResponses);
    }

}
