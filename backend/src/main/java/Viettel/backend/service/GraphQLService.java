package Viettel.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class GraphQLService {

    private static final String DATAHUB_GRAPHQL_URL = "http://localhost:8080/api/graphql";

    private final RestTemplate restTemplate;

    public GraphQLService() {
        this.restTemplate = new RestTemplate();
    }

    public String fetchDatabaseSchema(String databaseName) {
        // Create the GraphQL query string
        String query = String.format(
                "{ search(input: { type: DATASET, query: \\\"%s\\\", start: 0, count: 1000 }) { searchResults { entity { ... on Dataset { name schemaMetadata { fields { fieldPath description nativeDataType } } } } } } }",
                databaseName
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the JSON request body
        String jsonQuery = "{\"query\":\"" + query + "\"}";

        HttpEntity<String> entity = new HttpEntity<>(jsonQuery, headers);

        // Log the request details for debugging
        System.out.println("Request Headers: " + headers);
        System.out.println("Request Body: " + jsonQuery);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(DATAHUB_GRAPHQL_URL, HttpMethod.POST, entity, String.class);
            String response = responseEntity.getBody();
            System.out.println("Response: " + response);
            return extractSchemaMetadata(response);
        } catch (HttpClientErrorException e) {
            // Handle the error and log details for debugging
            System.err.println("HTTP Status: " + e.getStatusCode());
            System.err.println("Error Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Request to DataHub failed: " + e.getMessage(), e);
        }
    }

    private String extractSchemaMetadata(String response) {
        // Parse the JSON response and extract schema metadata
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder metadataBuilder = new StringBuilder();

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode searchResults = rootNode.path("data").path("search").path("searchResults");

            for (JsonNode result : searchResults) {
                String tableName = result.path("entity").path("name").asText();
                metadataBuilder.append("Table: ").append(tableName).append("\n");

                JsonNode fields = result.path("entity").path("schemaMetadata").path("fields");
                for (JsonNode field : fields) {
                    String fieldName = field.path("fieldPath").asText();
                    String dataType = field.path("nativeDataType").asText();
                    metadataBuilder.append("  - Field: ").append(fieldName).append(", Type: ").append(dataType).append("\n");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extracting schema metadata: " + e.getMessage(), e);
        }

        return metadataBuilder.toString();
    }
}
