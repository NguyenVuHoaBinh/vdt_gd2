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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GraphQLService {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLService.class);
    private static final String DATAHUB_GRAPHQL_URL = "http://localhost:8080/api/graphql";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GraphQLService() {
        this.restTemplate = new RestTemplate();
    }

    public String fetchDatabaseSchema(String databaseName) {
        logger.info("Fetching database schema for database: {}", databaseName);

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

        logger.debug("GraphQL request details - URL: {}, Headers: {}, Query: {}", DATAHUB_GRAPHQL_URL, headers, jsonQuery);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(DATAHUB_GRAPHQL_URL, HttpMethod.POST, entity, String.class);
            String response = responseEntity.getBody();
            logger.info("Received response for database: {}", databaseName);
            logger.debug("GraphQL response: {}", response);
            return extractSchemaMetadata(response);
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error while fetching database schema for database: {}. Status: {}, Error: {}", databaseName, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Request to DataHub failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error while fetching database schema for database: {}", databaseName, e);
            throw new RuntimeException("Request to DataHub failed: " + e.getMessage(), e);
        }
    }

    public String extractSchemaMetadata(String response) {
        logger.info("Extracting schema metadata from the response.");

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

                    fieldsArray.add(fieldObject);
                }

                tableObject.set("Fields", fieldsArray);
                tablesArray.add(tableObject);
            }

            logger.info("Successfully extracted schema metadata.");
        } catch (Exception e) {
            logger.error("Error extracting schema metadata from response.", e);
            throw new RuntimeException("Error extracting schema metadata: " + e.getMessage(), e);
        }

        // Convert the final JSON structure to a string
        try {
            String resultJson = objectMapper.writeValueAsString(tablesArray);
            logger.debug("Extracted schema metadata JSON: {}", resultJson);
            return resultJson;
        } catch (Exception e) {
            logger.error("Error converting schema metadata to JSON string.", e);
            throw new RuntimeException("Error converting to JSON string: " + e.getMessage(), e);
        }
    }
}
