package Viettel.backend.controller;

import Viettel.backend.AdvanceRAG.service.Chunker;
import Viettel.backend.AdvanceRAG.service.OpenAIEmbeddingService;
import Viettel.backend.AdvanceRAG.service.SearchService;
import Viettel.backend.agentState.TaskAnalysisService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
public class ShoppingChatController {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingChatController.class);

    @Autowired
    private DatabaseConfig databaseConfig;

    @Autowired
    private OpenAIEmbeddingService embeddingService;

    @Autowired
    private GraphQLService graphQLService;

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

    @Autowired
    private TtsService ttsService;

    @Autowired
    private TaskAnalysisService taskAnalysisService;


    private final Map<String, String> dbParamsStore = new HashMap<>();
    private JdbcTemplate jdbcTemplate;

    private String indexName="";

    //TODO: add memory to corrective schema entity
    @PostMapping("/vPOS_connect")
    public Map<String, Object> connect(@RequestBody Map<String, String> dbParams) {
        logger.info("Starting connection process for session ID: {}", dbParams.get("sessionId"));

        Map<String, Object> response = new HashMap<>();
        try {
            String sessionId = dbParams.get("sessionId");
            if (sessionId == null || sessionId.isEmpty()) {
                throw new IllegalArgumentException("Session ID is required");
            }
            indexName = sessionId;

            logger.debug("Creating indices for session ID: {}", sessionId);
            indexService.createSchemaMetadataIndex(indexName);
            indexService.createChatIndex();

            DataSource dataSource = databaseConfig.createDataSource(dbParams);
            this.jdbcTemplate = new JdbcTemplate(dataSource);
            dbParamsStore.putAll(dbParams);

            logger.debug("Generating ingestion configuration for session ID: {}", sessionId);
            dataHubIngestionService.generateIngestionConfig(dbParams);

            DataHubIngestionService.IngestionResult ingestionResult = dataHubIngestionService.runIngestionPipeline();
            response.put("ingestionResult", ingestionResult);

            String metadata = graphQLService.fetchDatabaseSchema(dbParams.get("database"));

            Map<String, Object> document = new HashMap<>();
            document.put("id", dbParams.get("database"));
            document.put("content", metadata);
            List<Map<String, Object>> documents = Collections.singletonList(document);

            logger.debug("Chunking documents for session ID: {}", sessionId);
            int chunkSize = 512;
            int overlap = 50;
            int minChunkSize = 50;
            List<Map<String, Object>> chunkedDocuments = chunker.sentenceWiseTokenizedChunkDocuments(
                    documents, chunkSize, overlap, minChunkSize);

            List<String> textsToEmbed = chunkedDocuments.stream()
                    .map(doc -> (String) doc.get("original_text"))
                    .collect(Collectors.toList());

            List<double[]> embeddings = embeddingService.getEmbeddings(textsToEmbed);

            for (int i = 0; i < chunkedDocuments.size(); i++) {
                chunkedDocuments.get(i).put("embedding", embeddings.get(i));
            }

            logger.debug("Indexing chunked documents into Elasticsearch for session ID: {}", sessionId);
            for (Map<String, Object> doc : chunkedDocuments) {
                MetadataDocument metadataDocument = new MetadataDocument();
                metadataDocument.setId((String) doc.get("id"));
                metadataDocument.setChunk((List<Integer>) doc.get("chunk"));
                metadataDocument.setOriginalText((String) doc.get("original_text"));
                metadataDocument.setChunkIndex((Integer) doc.get("chunk_index"));
                metadataDocument.setParentId((String) doc.get("parent_id"));
                metadataDocument.setChunkTokenCount((Integer) doc.get("chunk_token_count"));
                metadataDocument.setEmbedding((double[]) doc.get("embedding"));

                elasticsearchService.indexMetadataDocument(indexName, metadataDocument.getId(), metadataDocument);
            }

            response.put("sessionId", sessionId);
            response.put("success", true);
            logger.info("Connection process completed successfully for session ID: {}", sessionId);

        } catch (Exception e) {
            logger.error("Connection failed for session ID: {}", dbParams.get("sessionId"), e);
            response.put("success", false);
            response.put("message", "Connection failed: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/vPOS_chat")
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


//            String cachedResponse = exactCacheService.getExactCachedResponse(message);
//            if (cachedResponse != null) {
//                logger.info("Semantic cache hit, returning cached response.");
//                result.put("queryResult", cachedResponse);
//                return result;
//            }
//            // Check cache (optional)
//             String semnaticCachedResponse = semanticCacheService.performHybridSearch(message);
//             if (semnaticCachedResponse != null) {
//                 logger.info("Semantic cache hit, returning cached response.");
//                 result.put("fullResponse", semnaticCachedResponse);
//                 return result;
//             }

            // Retrieve previous chat history
            List<String> previousChats = chatMemoryService.getUserChat(sessionId);
            int maxMessages = 5; // Define a suitable limit
            List<String> recentChats = previousChats.stream()
                    .skip(Math.max(0, previousChats.size() - maxMessages))
                    .collect(Collectors.toList());

            StringBuilder conversationBuilder = new StringBuilder();
            for (String chatEntry : recentChats) {
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
//
//            // Step 1: Generate refined query and HyDE document
//            String refinedQuery = llmService.generateRefinedQuery(message, model);
//
//            // Step 2: Generate embedding for the hypothetical document
//            double[] hydeEmbedding = embeddingService.getEmbedding(refinedQuery);
//
//            // Step 3: Perform hybrid search
//            int numCandidates = 100;
//            int numResults = 4;
//
//            List<Map<String, Object>> searchResults = searchService.hybridSearch(
//                    indexName, refinedQuery, hydeEmbedding, numCandidates, numResults);

//            // Step 4: Collect context from search results
//            StringBuilder contextBuilder = new StringBuilder();
//            for (Map<String, Object> searchResult : searchResults) {
//                String originalText = (String) searchResult.get("originalText");
//                if (originalText != null) {
//                    contextBuilder.append(originalText).append("\n\n");
//                }
//            }
//            String context = contextBuilder.toString();

//          Product database
            String SQL_QUERY = " SELECT ProductName,price, currentstock FROM public.products";
            // Execute the SQL query and retrieve a list of result maps
            List<Map<String, Object>> sqlqueryResult = (List<Map<String, Object>>) sqlExecutionService.executeQuery(SQL_QUERY, dbParams);

            // Combine system role, schema metadata, and user message to create an enhanced prompt
            String combinedPrompt =
                    (systemRole != null ? systemRole : "") +
//                            "\n\nSchema Metadata:\n" + context +
                            sqlqueryResult +
                            "\n\nConversation History:\n" + conversationHistory +
                            "\n\nUser Query:\n" + message;

            logger.info("Enhanced Prompt: \n{}", combinedPrompt);

            // Process the message with the LLM using the enhanced prompt
            Map<String, String> processedResult = llmService.processMessage(message, model, combinedPrompt);
            String fullResponse = processedResult.get("fullResponse");
            String updateDatabase = processedResult.get("sqlQuery");

            if(!updateDatabase.isEmpty()){
                sqlExecutionService.executeUpdate(updateDatabase, dbParams);
                String finalResult = llmService.processAnalysis(fullResponse, model);

                //Get all related product information
                String productSQL = llmService.get_product_detail(finalResult,model);
                List<Map<String, Object>> productResult = (List<Map<String, Object>>) sqlExecutionService.executeQuery(productSQL, dbParams);
//                // Create invoice
//                String temp = productResult +"\n This is the invoice:\n" + finalResult;
//                Map<String, String> createInvoice = llmService.create_invoice(temp,model);
//                sqlExecutionService.executeQuery(createInvoice, dbParams);

                result.put("fullResponse",finalResult);
            }else{
                result.put("fullResponse",fullResponse);
            }
            //modify
            chatMemoryService.storeUserChat(sessionId, "user", message);
            chatMemoryService.storeUserChat(sessionId, "assistant", fullResponse);
//            // Write fullResponse to a temporary file
//            File tempFile = File.createTempFile("fullResponse", ".txt");
//            try (FileWriter writer = new FileWriter(tempFile)) {
//                writer.write(fullResponse);
//            }
//
//            // Pass the file path to the TTS service
//            byte[] speechAudio = ttsService.generateSpeech(tempFile.getAbsolutePath()); // Pass the file path instead of raw text
//
//            // Return fullResponse and Base64 encoded audio
//            result.put("fullResponse", fullResponse);
//            result.put("audio", Base64.getEncoder().encodeToString(speechAudio));
//
//            // Clean up temporary file if needed
//            tempFile.delete();


////            // Check if the response indicates "No suitable request"
//            if ("No suitable request, entity is not found in the given schema.".equalsIgnoreCase(fullResponse.trim())) {
//                logger.info("No suitable entity found, searching for related fields in schema.");
//
//                // Create a corrective prompt combining the original data
//                String schemaPrompt = String.format(
//                        """
//                                You are given the user query and schema that contain tables and fields, your job is to find any field name that related to the user query intention
//                                For example:
//
//                                User: "Give me all data about day active mon"
//                                Schema: "1\\",\\"Type\\":\\"TEXT\\"},{\\"Field\\":\\"day_active_mon_n\\",\\"Type\\":\\"DATE\\"},{\\"Field\\":\\"day_active_mon_n1\\",\\"Type\\":\\"TEXT\\"},{\\"Field\\":\\"phi_dk_data_phanbo_combo\\",\\"Type\\":\\"TEXT\\"}]},{\\"Table\\":\\"f_ccai_profile_telecom_mon\\",\\"Fields\\":[{\\"Field\\":\\"id\\",\\"Type\\":\\"INTEGER\\"},{\\"Field\\":\\"bq_cuoc_goc_6thang\\",\\"Type\\":\\"TEXT\\"}]},{\\"Table\\":\\"f_ccai_profile_telecom_month\\",\\"Fields\\":[{\\"Field\\":\\"id\\",\\"Type\\":\\"INTEGER\\"},{\\"Field\\":\\"tong_tieu_dung\\",\\"Type\\":\\"TEXT\\"},
//                                Response: You mean day_active_mon_n, day_active_mon_n1?
//
//                                If the field name existing in multiple table, please list also the table that the field name related to.
//
//                                For example:
//
//                                User: "Give me all data about day active mon"
//                                Schema: {"Table":"mb_pos_daily_new","Fields":[{"Field":"id","Type":"INTEGER"},{"Field":"tong_tieu_dung","Type":"TEXT"},{"Field":"ds_ctkm_potential_customer","Type":"TEXT"},{"Field":"goi_cuoc_data","Type":"TEXT"},{"Field":"ds_goi_addon","Type":"TEXT"}]},{"Table":"mb_pos_new","Fields":[{"Field":"id","Type":"INTEGER"},{"Field":"tong_tieu_dung","Type":"TEXT"}]}
//                                Response: You mean tong_tieu_dung in mb_pos_new or tong_tieu_dung in mb_pos_daily_new?
//
//                                START OF THE USER QUERY: '%s'
//
//                                START OF THE SCHEMA:
//                                '%s'
//                                """,
//                        message,context
//                );
//
//                // Process the corrective prompt with the LLM
//                Map<String, String> correctedResult = llmService.processMessage(message, model, schemaPrompt);
//
//
//                fullResponse = correctedResult.get("fullResponse");
//                result.put("fullResponse",fullResponse);
//                chatMemoryService.storeUserChat(sessionId, "assistant", fullResponse);
//                return result;
//            }
//
////             Loop for up to 3 attempts
//            while (attempt <= maxAttempts && !success) {
//                try {
//                    logger.info("Attempt " + attempt + ": Executing SQL Query: " + sqlQuery);
//
//                    // Execute the SQL query
//                    List<Map<String, Object>> queryResult = sqlExecutionService.executeQuery(sqlQuery, dbParams);
//                    result.put("sqlQuery", sqlQuery);
//                    result.put("queryResult", queryResult);
//                    success = true; // Mark success
//                    logger.info("SQL Query executed successfully.");
//
//
//                    result.put("sqlQuery", sqlQuery);
//                    result.put("queryResult", queryResult);
//
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    String queryResultJson = objectMapper.writeValueAsString(queryResult);
//
//                    Map<String, Object> dataToStore = new HashMap<>();
//                    dataToStore.put("sessionID", sessionId);
//                    dataToStore.put("message", message);
//                    dataToStore.put("queryResult", queryResultJson);
//
//                    elasticsearchService.indexChatDocument("chat_index", sessionId + "-chat", dataToStore);
//
//                    semanticCacheService.cacheResponse(message, queryResultJson);
//                    exactCacheService.cacheExactResponse(message, queryResultJson);
//
//                    chatMemoryService.storeUserChat(sessionId, "assistant", fullResponse);
//
//                    userChatService.saveUserChat(sessionId, message, queryResultJson);
//
//
//                } catch (Exception e) {
//                    logger.error("Attempt " + attempt + ": SQL Execution failed: " + e.getMessage());
//
//                    if (attempt == maxAttempts) {
//                        result.put("error", "SQL execution failed after " + maxAttempts + " attempts: " + e.getMessage());
//                        break;
//                    }
//
//                    // Collect the error information
//                    String errorLog = e.getMessage();
//
//                    // Create a corrective prompt combining the original data
//                    String correctivePrompt = String.format(
//                            "'%s' \n\nThe original user request was: '%s'. The generated SQL query was: '%s'. The error encountered was: '%s'. The related schema is: '%s'. Please correct the SQL query based on this information. You MUST return the corrected SQL query only and DO NOT EXPLAIN ANYTHING.",
//                            systemRole, message, sqlQuery, errorLog, context
//                    );
//
//                    // Process the corrective prompt with the LLM
//                    Map<String, String> correctedResult = llmService.processMessage(message, model, correctivePrompt);
//
//                    sqlQuery = correctedResult.get("sqlQuery");
//
//                    if (sqlQuery == null || sqlQuery.isEmpty()) {
//                        logger.error("No corrected SQL query generated by LLM on attempt " + attempt);
//                        result.put("error", "Failed to generate a corrected SQL query on attempt " + attempt);
//                        break;
//                    }
//
//                }
//                attempt++;
//            }
//
//            if (!success) {
//                logger.error("SQL execution ultimately failed after " + maxAttempts + " attempts.");
//            }

        } catch (Exception e) {
            logger.error("Error processing chat", e);
            result.put("error", "Failed to process the request: " + e.getMessage());
        }

        return result;
    }
}


