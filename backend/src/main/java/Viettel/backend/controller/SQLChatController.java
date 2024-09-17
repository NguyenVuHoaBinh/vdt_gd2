package Viettel.backend.controller;

import Viettel.backend.AdvanceRAG.service.Chunker;
import Viettel.backend.AdvanceRAG.service.LLMService_advanceRAG;
import Viettel.backend.AdvanceRAG.service.OpenAIEmbeddingService;
import Viettel.backend.AdvanceRAG.service.SearchService;
import Viettel.backend.config.databaseconfig.DatabaseConfig;
import Viettel.backend.model.MetadataDocument;
import Viettel.backend.service.SQLExecutionService;
import Viettel.backend.service.UserChatService;
import Viettel.backend.service.cacheservice.ExactCacheService;
import Viettel.backend.service.cacheservice.SemanticCacheService;
import Viettel.backend.service.datahubservice.DataHubIngestionService;
import Viettel.backend.service.elasticsearch.ElasticsearchService;
import Viettel.backend.service.elasticsearch.IndexService;
import Viettel.backend.service.elasticsearch.SearchAndRerankService;
import Viettel.backend.service.llmservice.ChatMemoryService;
import Viettel.backend.service.llmservice.LLMService;
import Viettel.backend.service.metadataservice.GraphQLService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// Additional imports
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
public class SQLChatController {

    private static final Logger logger = LoggerFactory.getLogger(SQLChatController.class);

    @Autowired
    private DatabaseConfig databaseConfig;

    @Autowired
    private OpenAIEmbeddingService embeddingService;

    @Autowired
    private GraphQLService graphQLService;

    @Autowired
    private LLMService_advanceRAG llmService_advanceRAG;

    @Autowired
    private LLMService llmService;

    @Autowired
    private DataHubIngestionService dataHubIngestionService;

    @Autowired
    private SQLExecutionService sqlExecutionService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ChatMemoryService chatMemoryService;

    @Autowired
    private ExactCacheService exactCacheService;

    @Autowired
    private SemanticCacheService semanticCacheService;

    @Autowired
    private Chunker chunker;

    @Autowired
    private SearchService searchService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private UserChatService userChatService;

    @Autowired
    private SearchAndRerankService searchAndRerankService;


    private final Map<String, String> dbParamsStore = new HashMap<>();
    private JdbcTemplate jdbcTemplate;

    private String indexName="";

    @PostMapping("/connect")
    public Map<String, Object> connect(@RequestBody Map<String, String> dbParams) {
        Map<String, Object> response = new HashMap<>();
        try {
            String sessionId = dbParams.get("sessionId");
            if (sessionId == null || sessionId.isEmpty()) {
                throw new IllegalArgumentException("Session ID is required");
            }
            indexName = sessionId;

            indexService.createSchemaMetadataIndex(indexName);
            indexService.createChatIndex();

            // Step 1: Create DataSource and set up JdbcTemplate
            DataSource dataSource = databaseConfig.createDataSource(dbParams);
            this.jdbcTemplate = new JdbcTemplate(dataSource);

            // Step 2: Store DB parameters for later use
            dbParamsStore.putAll(dbParams);

            // Step 3: Generate the ingestion configuration file
            dataHubIngestionService.generateIngestionConfig(dbParams);

            // Step 4: Run the ingestion pipeline and capture the result
            DataHubIngestionService.IngestionResult ingestionResult = dataHubIngestionService.runIngestionPipeline();
            response.put("ingestionResult", ingestionResult);

            // Step 5: Fetch metadata from the GraphQL service
            String metadata = graphQLService.fetchDatabaseSchema(dbParams.get("database"));

            // Step 6: Prepare documents for chunking
            Map<String, Object> document = new HashMap<>();
            document.put("id", dbParams.get("database"));
            document.put("content", metadata);
            List<Map<String, Object>> documents = Collections.singletonList(document);

            // Step 7: Chunk the documents
            int chunkSize = 512;
            int overlap = 50;
            int minChunkSize = 50;
            List<Map<String, Object>> chunkedDocuments = chunker.sentenceWiseTokenizedChunkDocuments(
                    documents, chunkSize, overlap, minChunkSize);

            // Step 8: Collect all texts to embed
            List<String> textsToEmbed = new ArrayList<>();
            for (Map<String, Object> doc : chunkedDocuments) {
                String textContent = (String) doc.get("original_text");
                textsToEmbed.add(textContent);
            }

            // Step 9: Generate embeddings in batch
            List<double[]> embeddings = embeddingService.getEmbeddings(textsToEmbed);

            // Step 10: Assign embeddings back to chunked documents
            for (int i = 0; i < chunkedDocuments.size(); i++) {
                chunkedDocuments.get(i).put("embedding", embeddings.get(i));
            }

            // Step 11: Index the chunked documents into Elasticsearch
            for (Map<String, Object> doc : chunkedDocuments) {
                String id = (String) doc.get("id");
                List<Integer> chunk = (List<Integer>) doc.get("chunk");

                String originalText = (String) doc.get("original_text");
                Integer chunkIndex = (Integer) doc.get("chunk_index");
                String parentId = (String) doc.get("parent_id");
                Integer chunkTokenCount = (Integer) doc.get("chunk_token_count");
                double[] embedding = (double[]) doc.get("embedding"); // Use the embedding as double[]

                // Create a MetadataDocument instance
                MetadataDocument metadataDocument = new MetadataDocument();
                metadataDocument.setId(id);
                metadataDocument.setChunk(chunk);
                metadataDocument.setOriginalText(originalText);
                metadataDocument.setChunkIndex(chunkIndex);
                metadataDocument.setParentId(parentId);
                metadataDocument.setChunkTokenCount(chunkTokenCount);
                metadataDocument.setEmbedding(embedding); // Set the embedding as double[]

                // Index the MetadataDocument into Elasticsearch
                elasticsearchService.indexMetadataDocument(indexName, id, metadataDocument);
            }

            // Step 12: Set success response
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
            String sessionId = (String) chatParams.get("sessionId");

            if (sessionId == null || sessionId.isEmpty()) {
                throw new IllegalArgumentException("Session ID is required");
            }

            // Retrieve stored dbParams
            Map<String, String> dbParams = dbParamsStore;

            if (dbParams == null || dbParams.isEmpty()) {
                logger.error("No database connection parameters found. Please connect first.");
                result.put("error", "No database connection parameters found. Please connect first.");
                return result;
            }

            // Retrieve previous chat history
            List<String> previousChats = chatMemoryService.getUserChat(sessionId);
            int maxMessages = 3; // Define a suitable limit
            List<String> recentChats = previousChats.stream()
                    .skip(Math.max(0, previousChats.size() - maxMessages))
                    .collect(Collectors.toList());

            StringBuilder conversationBuilder = new StringBuilder();
            for (String chatEntry : recentChats) {
                // Assuming chat entries are formatted as "user:timestamp:message" or "assistant:timestamp:message"
                String[] parts = chatEntry.split(":", 3);
                if (parts.length == 3) {
                    String role = parts[0];
                    String userMessage = parts[2];
                    conversationBuilder.append(role).append(": ").append(userMessage).append("\n");
                }
            }

            // Append the new user message
            conversationBuilder.append("user: ").append(message).append("\n");

            String conversationHistory = conversationBuilder.toString();
            logger.debug("Conversation History:\n{}", conversationHistory);



            // Check Exact Cache
//            String cachedResponse = exactCacheService.getExactCachedResponse(message);
//            if (cachedResponse != null) {
//                logger.info("Exact cache hit, returning cached response.");
//                result.put("queryResult", cachedResponse);
//                // Store in chat history
//                chatMemoryService.storeUserChat(sessionId, "user", message);
//                chatMemoryService.storeUserChat(sessionId, "assistant", cachedResponse);
//                return result;
////            } else {
////                // Check Semantic Cache
////                cachedResponse = semanticCacheService.performHybridSearch(message);
////                if (cachedResponse != null) {
////                    logger.info("Semantic cache hit, returning cached response.");
////                    result.put("queryResult", cachedResponse);
////                    // Store in chat history
////                    chatMemoryService.storeUserChat(sessionId, "user", message);
////                    chatMemoryService.storeUserChat(sessionId, "assistant", cachedResponse);
////                    return result;
////                }
//            }

            // Retrieve and embed database schema
            String combinedPrompt="";
            // Step 1: Generate refined query and HyDE document
            String refinedQuery = llmService.generateRefinedQuery(message,model);
            String hydeDocument = llmService.generateHyDE(message,model);

            // Step 2: Generate embedding for the hypothetical document
            double[] hydeEmbedding = embeddingService.getEmbedding(hydeDocument);

            // Step 3: Perform hybrid search
            int numCandidates = 100;
            int numResults = 20;

            List<Map<String, Object>> searchResults = searchService.hybridSearch(
                    indexName, refinedQuery, hydeEmbedding, numCandidates, numResults);

            // Step 4: Collect context from search results
            StringBuilder contextBuilder = new StringBuilder();
            for (Map<String, Object> searchResult : searchResults) {
                String originalText = (String) searchResult.get("originalText");
                if (originalText != null) {
                    contextBuilder.append(originalText).append("\n\n");
                }
            }
            String context = contextBuilder.toString();

            // Combine system role, schema metadata, and user message to create an enhanced prompt
            combinedPrompt = "\n\nConversation History:\n" + conversationHistory + (systemRole != null ? systemRole : "") +
                    "\n\nSchema Metadata:\n" + context  +
                    "\n\nUser Query:\n" + message;

            logger.info("Enhanced Prompt: \n{}", combinedPrompt);

            // Process the message with the LLM using the enhanced prompt
            Map<String, String> processedResult = llmService.processMessage(message, model, combinedPrompt);

            String fullResponse = processedResult.get("fullResponse");
            String sqlQuery = processedResult.get("sqlQuery");

            result.put("fullResponse", fullResponse);
            logger.info("Full Response: " + fullResponse);

            // Execute the SQL query if available
            if (sqlQuery != null && !sqlQuery.isEmpty()) {
                logger.info("Extracted SQL Query: " + sqlQuery);
                List<Map<String, Object>> queryResult = sqlExecutionService.executeQuery(sqlQuery, dbParams);

                result.put("sqlQuery", sqlQuery);
                result.put("queryResult", queryResult);

                // Convert queryResult to JSON String
                ObjectMapper objectMapper = new ObjectMapper();
                String queryResultJson = objectMapper.writeValueAsString(queryResult);

                // Prepare data to store in Elasticsearch
                Map<String, Object> dataToStore = new HashMap<>();
                dataToStore.put("sessionID", sessionId);
                dataToStore.put("message", message);
                dataToStore.put("queryResult", queryResultJson);

                elasticsearchService.indexChatDocument("chat_index", sessionId + "-chat", dataToStore);

                // Cache the response in the semantic cache
                semanticCacheService.cacheResponse(message, queryResultJson);
                exactCacheService.cacheExactResponse(message,queryResultJson);

                // Store assistant's response in chat history
                chatMemoryService.storeUserChat(sessionId, "assistant", fullResponse);

                // Store chat user in PostgreSQL
                userChatService.saveUserChat(sessionId,message,queryResultJson);
            } else {
                result.put("sqlQuery", "No SQL query found in the response.");
                chatMemoryService.storeUserChat(sessionId, "assistant", fullResponse);
            }

            // ... (Other methods remain unchanged)
        } catch (Exception e) {
            logger.error("Error processing chat", e);
            result.put("error", "Failed to process the request: " + e.getMessage());
        }

        return result;
    }
}


