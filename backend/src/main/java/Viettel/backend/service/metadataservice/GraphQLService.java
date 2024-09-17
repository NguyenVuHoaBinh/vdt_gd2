package Viettel.backend.service.metadataservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final ObjectMapper objectMapper = new ObjectMapper();


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

    public String extractSchemaMetadata(String response) {
        // Create a root array node to store table objects
        ArrayNode tablesArray = objectMapper.createArrayNode();

        try {
            // Parse the JSON response
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode searchResults = rootNode.path("data").path("search").path("searchResults");

            // Loop through each result to extract schema details
            for (JsonNode result : searchResults) {
                // Create a JSON object for each table
                ObjectNode tableObject = objectMapper.createObjectNode();
                String tableName = result.path("entity").path("name").asText();
                tableObject.put("Table", tableName);

                // Create an array to hold field objects
                ArrayNode fieldsArray = objectMapper.createArrayNode();
                JsonNode fields = result.path("entity").path("schemaMetadata").path("fields");

                for (JsonNode field : fields) {
                    // Create a JSON object for each field
                    ObjectNode fieldObject = objectMapper.createObjectNode();
                    String fieldName = field.path("fieldPath").asText();
                    String dataType = field.path("nativeDataType").asText();

                    fieldObject.put("Field", fieldName);
                    fieldObject.put("Type", dataType);

                    // Add the field object to the fields array
                    fieldsArray.add(fieldObject);
                }

                // Add fields array to the table object
                tableObject.set("Fields", fieldsArray);

                // Add table object to the tables array
                tablesArray.add(tableObject);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extracting schema metadata: " + e.getMessage(), e);
        }

        // Convert the final JSON structure to a string
        try {
            return objectMapper.writeValueAsString(tablesArray);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to JSON string: " + e.getMessage(), e);
        }
    }
}
