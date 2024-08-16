package Viettel.backend.controller;

import Viettel.backend.service.SQLExecutionService;
import Viettel.backend.service.DataHubIngestionService;
import Viettel.backend.config.DatabaseConfig;
import Viettel.backend.service.GraphQLService;
import Viettel.backend.service.LLMService;
import Viettel.backend.service.VectorDBService;
import Viettel.backend.service.ChatMemoryService;
import Viettel.backend.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin("*")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private DatabaseConfig databaseConfig;

    @Autowired
    private GraphQLService graphQLService;

    @Autowired
    private LLMService llmService;

    @Autowired
    private DataHubIngestionService dataHubIngestionService;

    @Autowired
    private SQLExecutionService sqlExecutionService;

    @Autowired
    private VectorDBService vectorDBService;

    @Autowired
    private ChatMemoryService chatMemoryService;

    @Autowired
    private CacheService cacheService;

    private Map<String, String> dbParamsStore = new HashMap<>();  // In-memory store for dbParams
    private JdbcTemplate jdbcTemplate;

    // Method to generate a session ID
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    @PostMapping("/connect")
    public Map<String, Object> connect(@RequestBody Map<String, String> dbParams) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Generate a session ID if not provided
            String sessionId = dbParams.getOrDefault("sessionId", generateSessionId());

            // Create DataSource and set up JdbcTemplate
            DataSource dataSource = databaseConfig.createDataSource(dbParams);
            this.jdbcTemplate = new JdbcTemplate(dataSource);

            // Store the dbParams for later use in the chat method
            dbParamsStore.putAll(dbParams);

            // Generate the ingestion configuration file
            dataHubIngestionService.generateIngestionConfig(dbParams);

            // Run the ingestion pipeline and capture the result
            DataHubIngestionService.IngestionResult ingestionResult = dataHubIngestionService.runIngestionPipeline();
            response.put("ingestionResult", ingestionResult);

            // Fetch metadata
            String metadata = graphQLService.fetchDatabaseSchema(dbParams.get("database"));
            response.put("metadata", metadata);

            // Store metadata in the Redis vector store
            String metadataId = "metadata-" + dbParams.get("database"); // Create a unique ID for metadata
            Map<String, String> metadataMap = new HashMap<>();
            metadataMap.put("database", dbParams.get("database"));
            metadataMap.put("metadata", metadata);
            metadataMap.put("dbType", dbParams.get("dbType")); // Assuming dbType is part of dbParams

            // Store chat session metadata
            chatMemoryService.storeSessionMetadata(sessionId, metadataMap);

            // Store the initial connection message in chat history
            chatMemoryService.storeUserChat(sessionId, "System", "Database connected.");

            // Set success to true and return session ID
            response.put("sessionId", sessionId);
            response.put("success", true);
        } catch (Exception e) {
            logger.error("Connection failed", e);
            response.put("success", false);
            response.put("message", "Connection failed: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> chatParams) {
        Map<String, Object> result = new HashMap<>();
        try {
            String message = (String) chatParams.get("message");
            String model = (String) chatParams.get("model");
            String systemRole = (String) chatParams.get("systemRole");
            String metadata = (String) chatParams.get("metadata");
            String sessionId = (String) chatParams.get("sessionId");

            // Retrieve stored dbParams
            Map<String, String> dbParams = dbParamsStore;

            if (dbParams == null || dbParams.isEmpty()) {
                logger.error("No database connection parameters found. Please connect first.");
                result.put("error", "No database connection parameters found. Please connect first.");
                return result;
            }

            // Perform semantic search using the combined user query and metadata
            String searchResult = vectorDBService.semanticSearch(message, metadata);

            // Combine system role, search result, and user message to create a prompt
            String combinedPrompt = (systemRole != null ? systemRole : "") + "\n\n" + searchResult + "\n\n" + message;
            logger.info("Combined Prompt: " + combinedPrompt);

            // Check cache for previous similar query
            String cachedResponse = cacheService.getCachedResponse(combinedPrompt);
            if (cachedResponse != null) {
                logger.info("Cache hit, returning cached response.");
                result.put("fullResponse", cachedResponse);
                return result;
            }

            // Process the message with the LLM
            Map<String, String> processedResult = llmService.processMessage(message, model, combinedPrompt);
            String fullResponse = processedResult.get("fullResponse");
            String sqlQuery = processedResult.get("sqlQuery");

            result.put("fullResponse", fullResponse);
            logger.info("Full Response: " + fullResponse);

            if (sqlQuery != null && !sqlQuery.isEmpty()) {
                logger.info("Extracted SQL Query: " + sqlQuery);

                // Execute the extracted SQL query with stored dbParams
                List<Map<String, Object>> queryResult = sqlExecutionService.executeQuery(sqlQuery, dbParams);

                result.put("sqlQuery", sqlQuery);
                result.put("queryResult", queryResult);
            } else {
                result.put("sqlQuery", "No SQL query found in the response.");
            }

            // Store the user chat session in the Redis vector store
            chatMemoryService.storeUserChat(sessionId, "User", message);
            chatMemoryService.storeUserChat(sessionId, "System", fullResponse);

            // Cache the response for future similar queries
            cacheService.cacheResponse(combinedPrompt, fullResponse);

        } catch (Exception e) {
            logger.error("Error processing chat", e);
            result.put("error", "Failed to process the request: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/upload")
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        try {
            // Define the directory where files will be saved
            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            // Create the directory if it doesn't exist
            Path directoryPath = Paths.get(uploadDir);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Save the file to the specified directory
            Path filePath = directoryPath.resolve(file.getOriginalFilename());
            logger.info("Saving file to: " + filePath.toAbsolutePath());
            file.transferTo(filePath.toFile());

            response.put("response", "File uploaded successfully: " + file.getOriginalFilename());
        } catch (IOException e) {
            logger.error("File upload failed", e);
            response.put("response", "File upload failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred", e);
            response.put("response", "An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }
}
