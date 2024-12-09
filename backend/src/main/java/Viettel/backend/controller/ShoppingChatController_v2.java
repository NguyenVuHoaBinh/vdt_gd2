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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@RestController
@CrossOrigin("*")
public class ShoppingChatController_v2 {
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

    @Autowired
    private TextSpeechController textSpeechController;

    @Autowired
    private viAnController viancontroller;


    private final Map<String, String> dbParamsStore = new HashMap<>();
    private JdbcTemplate jdbcTemplate;

    private String indexName="";

    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedDateTime = now.format(formatter);

    //TODO: add memory to corrective schema entity
    //
    @PostMapping("/v2/vPOS_connect")
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
    @PostMapping("/v2/vPOS_chat")
    //TODO: CDC pipeline to update realtime from postgresql to elasticsearch
    //TODO: add TTS to all component
    //TODO: add Json to all component
    public Map<String, Object> chat(@RequestBody Map<String, Object> chatParams) {
        String FINAL_ORDER = "order";
        String FINAL_CUSTOMER_INFO = "customer";
        String FINAL_INVOICE = "invoice";
        String FINAL_INVOICE_ID = "invoice_id";
        String FINAL_PAYMENT = "payment";
        String FINAL_PRODUCT_INFO = "product_info";
        String FINAL_CHECK_DEBT_INFO = "check_debt_info";
        String FINAL_JSON="json";
        String FINAL_JSON_IMPORT="jsonImport";
        String FINAL_JSON_ADJUST="jsonAdjust";
        String FINAL_JSON_BA = "jsonBA";
        String FINAL_JSON_DEBT = "jsonDebt";
        String FINAL_JSON_INACTIVE = "jsonInactive";
        String FINAL_JSON_RESTOCK = "jsonRestockAlert";
        String FINAL_JSON_SELLING_TREND = "jsonTrend";
        LocalDate currentDate = LocalDate.now();
        ObjectMapper objectMapper = new ObjectMapper();




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

            String fullDetailProductSQL = "SELECT * FROM products";
            String fullDetailProductResult = sqlExecutionService.executeQuery(fullDetailProductSQL,dbParams).toString();

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


//          Product database
            String SQL_QUERY = " SELECT product_id,ProductName,selling_price, currentstock FROM public.products";
            // Execute the SQL query and retrieve a list of result maps
            List<Map<String, Object>> sqlqueryResult = (List<Map<String, Object>>) sqlExecutionService.executeQuery(SQL_QUERY, dbParams);

            // Combine system role, schema metadata, and user message to create an enhanced prompt
            String combinedPrompt =
                    (systemRole != null ? systemRole : "") +
//                            "\n\nSchema Metadata:\n" + context +
                            sqlqueryResult +
                            "\n\nConversation History:\n" + conversationHistory;

            logger.info("Enhanced Prompt: \n{}", combinedPrompt);
            chatMemoryService.storeUserChat(sessionId, "user", message);

            // Task analysis
            //TODO: APPLY WITH ReAct
            // TODO: CREATE STORED PROCEDURE
            int maxM = 1; // Define a suitable limit
            List<String> lastChats = previousChats.stream()
                    .skip(Math.max(0, previousChats.size() - maxM))
                    .collect(Collectors.toList());

            StringBuilder Builder = new StringBuilder();
            for (String chatEntry : lastChats) {
                String[] parts = chatEntry.split(":", 3);
                if (parts.length == 3) {
                    String role = parts[0];
                    String userMessage = parts[2];
                    Builder.append(role).append(": ").append(userMessage).append("\n");
                }
            }

            // Append the new user message
            Builder.append("user: ").append(message).append("\n");

            String lastHistory = Builder.toString();

            String analysisResponse = llmService.taskAnalysis(lastHistory,model);
            // Switch case here
            System.out.println(analysisResponse);
            switch (analysisResponse.toLowerCase()) {
                //TODO: add check isOrder
                //TODO: search product on elastic using hybrid search
                case "greeting":

                    break;
                case "order":
                    String processedOrder = llmService.processOrder(combinedPrompt, model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", processedOrder);
                    String isOrderCheck = llmService.isOrder(processedOrder,model);
                    if(isOrderCheck.toLowerCase().contains("true")) {
                        chatMemoryService.storeEntityData(sessionId, FINAL_ORDER, processedOrder);
                    }
                    //TODO:Add speech
                    String oOriginalSpeech = llmService.audioSmoothTranslation(processedOrder, model);
                    String pSpeech = viancontroller.sendTextVIAN(oOriginalSpeech);
                    result.put("audio", pSpeech);
                    result.put("fullResponse", processedOrder);

                    //TODO: add json infobill
                    String oIsOrder = llmService.isOrder(processedOrder,model);
                    if(oIsOrder.equalsIgnoreCase("true")){
                        String oJsonString = llmService.jsonConverter(processedOrder,model);
                        JsonNode ojsonNode = objectMapper.readTree(oJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON,ojsonNode.toString());
                        result.put("billInfo",ojsonNode);
                    }
                    break;
                case "check_customer":
                    //Customer check
                    //TODO: add check isCustomer
                    //TODO: revise checking customer based on name
                    //TODO: search customer on elastic using hybrid search
                    String databaseCheck = "\nThis is the information of customer retrieved from the database\n";
                    String customerInfoSQL  = llmService.checkCustomer(lastHistory,model);
                    List<Map<String, Object>> customerResult = (List<Map<String, Object>>) sqlExecutionService.executeQuery(customerInfoSQL, dbParams);
                    if(customerResult.isEmpty()){
                        databaseCheck = databaseCheck + "Customer is not found!";
                    }else{
                        databaseCheck = databaseCheck + customerResult;
                        chatMemoryService.storeEntityData(sessionId, FINAL_CUSTOMER_INFO, String.valueOf(customerResult));

                    }
                    Map<String, String>  CustomerInfo = llmService.createCustomerSQL(lastHistory + databaseCheck,model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", CustomerInfo.get("fullResponse"));

                    //TODO:Add speech
                    String ccOriginalSpeech = llmService.audioSmoothTranslation(CustomerInfo.get("fullResponse"), model);
                    String ccSpeech = viancontroller.sendTextVIAN(ccOriginalSpeech);
                    result.put("audio", ccSpeech);

                    result.put("fullResponse", CustomerInfo.get("fullResponse"));
                    //TODO: add json infobill

                    String ccIsCustomer = llmService.isCustomerInfo(CustomerInfo.get("fullResponse"),model);
                    if(ccIsCustomer.equalsIgnoreCase("true")){
                        String ccPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON).toString();
                        String ccJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                + ccPrevJson
                                + "\n Đây là thông tin khách hàng: \n"+
                                CustomerInfo.get("fullResponse");
                        String ccJsonString = llmService.jsonConverter(ccJsonCombined,model);
                        JsonNode ccjsonNode = objectMapper.readTree(ccJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON,ccjsonNode.toString());
                        result.put("billInfo",ccjsonNode);
                    }
                    break;

                //TODO: add check isCustomer
                //TODO: Revise creating customer
                //TODO: test function tts and json
                case "create_customer":
                    //Customer check
                    Map<String, String> createCustomerInfo = llmService.createCustomerSQL(conversationHistory,model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", createCustomerInfo.get("fullResponse"));


                    String sqlCustomer = createCustomerInfo.get("sqlQuery");
                    String ccFullResponse = createCustomerInfo.get("fullResponse");

                    // run query & remove sql from response
                    if(!sqlCustomer.isEmpty()) {
                        String ccCustomerInfo = sqlExecutionService.executeQuery(sqlCustomer,dbParams).toString();
                        String extractedResponse = llmService.processAnalysis(ccFullResponse,model);
                        chatMemoryService.storeEntityData(sessionId, FINAL_CUSTOMER_INFO, String.valueOf(ccCustomerInfo));
                        result.put("fullResponse", extractedResponse);
                    }
                    else{
                        result.put("fullResponse", ccFullResponse);
                    }
                    //TODO:Add speech
                    String crcOriginalSpeech = llmService.audioSmoothTranslation(result.get("fullResponse").toString(), model);
                    String crcSpeech = viancontroller.sendTextVIAN(crcOriginalSpeech);
                    result.put("audio", crcSpeech);

                    //TODO: add json
                    String isCreateCustomerCheck = llmService.isCustomerInfo(result.get("fullResponse").toString(),model);
                    if(isCreateCustomerCheck.equalsIgnoreCase("true")){
                        String crcPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON).toString();
                        String crcJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                + crcPrevJson
                                + "\n Đây là thông tin khách hàng: \n"+
                                result.get("fullResponse");
                        String crcJsonString = llmService.jsonConverter(crcJsonCombined,model);
                        JsonNode crcjsonNode = objectMapper.readTree(crcJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON,crcjsonNode.toString());
                        result.put("billInfo",crcjsonNode);
                    }

                    break;

                //TODO: add check isInvoice
                case "invoice":
                    //TODO: extract order and store
                    //TODO:update cancel invoice
                    //get full item data
                    String finalOrder = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_ORDER));
                    String finalCustomerInfo = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_CUSTOMER_INFO));

                    String productSQL = llmService.get_product_detail(finalOrder,model);
                    List<Map<String, Object>> productResult = (List<Map<String, Object>>) sqlExecutionService.executeQuery(productSQL, dbParams);

                    chatMemoryService.storeEntityData(sessionId,FINAL_PRODUCT_INFO,productResult.toString());

                    String finalMessage = lastHistory +"\n Đây là thông tin sản phẩm:  \n" + productResult + "\n Đây là thông tin mua hàng: \n " +finalOrder + "\n Đây là thông tin khách hàng \n "+finalCustomerInfo;
                    String invoiceResult = llmService.create_invoice(finalMessage,model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", invoiceResult);
                    chatMemoryService.storeEntityData(sessionId,FINAL_INVOICE,invoiceResult);

                    result.put("fullResponse", invoiceResult);

                    //TODO:Add speech
                    String iOriginalSpeech = llmService.audioSmoothTranslation(invoiceResult, model);
                    String iSpeech = viancontroller.sendTextVIAN(iOriginalSpeech);
                    result.put("audio", iSpeech);

                    //TODO: add json
                    String isInvoiceCheck = llmService.isInvoice(result.get("fullResponse").toString(),model);
                    if(isInvoiceCheck.equalsIgnoreCase("true")){
                        String iPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON).toString();
                        String iJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                + iPrevJson
                                + "\n Đây là thông tin hóa đơn: \n"+
                                invoiceResult+
                                "\n Thời gian ngày tháng lập hóa đơn hiện tại:\n"+
                                formattedDateTime+"\n";
                        String iJsonString = llmService.jsonConverter(iJsonCombined,model);
                        JsonNode ijsonNode = objectMapper.readTree(iJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON,ijsonNode.toString());
                        result.put("billInfo",ijsonNode);
                    }
                    break;
                //TODO: add check isPayment
                case "check_payment":
                    //Create invoice:
                    String cpFinalInvoice = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_INVOICE));
                    String cpFinalCustomerInfo = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_CUSTOMER_INFO));
                    String cpCombinedPrompt = "\nThis is invoice information: \n" +
                            cpFinalInvoice +
                            "\nThis is customer information: \n" +
                            cpFinalCustomerInfo;
                    String invoiceSQL = llmService.createInvoiceSQL(cpCombinedPrompt, model);
                    if(!invoiceSQL.isEmpty()){
                        String invoiceID = sqlExecutionService.executeQuery(invoiceSQL,dbParams).toString();
                        chatMemoryService.storeEntityData(sessionId,FINAL_INVOICE_ID,invoiceID);
                        //TODO: Add product invoice detail
                    }

                    String checkPaymentResult = llmService.processPayment(lastHistory, model);
                    chatMemoryService.storeUserChat(sessionId,"assistant",checkPaymentResult);
                    result.put("fullResponse",checkPaymentResult);

                    //TODO:Add speech
                    String cpOriginalSpeech = llmService.audioSmoothTranslation(checkPaymentResult, model);
                    String cpSpeech = viancontroller.sendTextVIAN(cpOriginalSpeech);
                    result.put("audio", cpSpeech);

                    break;

                case "immediately_payment":
                    String ipFinalInvoice = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_INVOICE));
                    String ipFinalCustomerInfo = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_CUSTOMER_INFO));
                    String ipFinalProductDetail = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_PRODUCT_INFO));
                    String ipFinalInvoiceID = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_INVOICE_ID));
                    String ipCombinedPrompt = "\nThis is full product information: \n" +
                            ipFinalProductDetail +
                            "\nThis is invoice information: \n" +
                            ipFinalInvoice +
                            "\nThis is customer information: \n" +
                            ipFinalCustomerInfo +
                            "\nThis is conversation between user and assistant: \n" +
                            ipFinalInvoiceID +
                            "\n" +
                            lastHistory
                            ;

                    Map<String, String> paymentResult = llmService.createImmediatelyPayment(ipCombinedPrompt, model);
                    String ipSQLcommand = paymentResult.get("sqlQuery");
                    String ipFullResponse = paymentResult.get("fullResponse");
                    if(!ipSQLcommand.isEmpty()){
                        Integer executeCheck = sqlExecutionService.executeUpdate(ipSQLcommand,dbParams);
                        String ipFinalResponse = llmService.processAnalysis(ipFullResponse,model);
                        if(executeCheck == 1){
                            //TODO:update invoice to paid
                            String ipUpdateInvoiceSQL = llmService.updatePaidInvoiceSQL(ipFinalInvoiceID,model);
                            sqlExecutionService.executeUpdate(ipUpdateInvoiceSQL,dbParams);
                            //TODO: generate invoice detail
                            String executeInvoiceDetailSQL = llmService.createInvoiceDetail(ipCombinedPrompt,model);
                            sqlExecutionService.executeUpdate(executeInvoiceDetailSQL,dbParams);

                            //TODO:update stock
                            String ipFinalProductInfo = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_PRODUCT_INFO));
                            String ipUpdateStock = "\nThis is invoice information: \n" +
                                    ipFinalInvoice +
                                    "\nThis is product information(take the number): \n" +
                                    ipFinalProductInfo;
                            String sqlUpdateStock = llmService.updateStock(ipUpdateStock,model);
                            sqlExecutionService.executeUpdate(sqlUpdateStock,dbParams);

                            //TODO: update customer last shopping date
                            String iCustomerInfo = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_CUSTOMER_INFO));
                            String iUpdateCustomerLastShoppingSQL = llmService.updateLastShopping(iCustomerInfo,model);
                            sqlExecutionService.executeQuery(iUpdateCustomerLastShoppingSQL,dbParams);



                            //TODO: add json
                            String ipPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON).toString();
                            String ipJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                    + ipPrevJson
                                    + "\n Đây là thông tin phương thức thanh toán: \n"+
                                    ipFullResponse;
                            String ipJsonString = llmService.jsonConverter(ipJsonCombined,model);
                            JsonNode ipjsonNode = objectMapper.readTree(ipJsonString);
                            chatMemoryService.storeEntityData(sessionId,FINAL_JSON,ipjsonNode.toString());
                            result.put("billInfo",ipjsonNode);

                        }
                        result.put("fullResponse",ipFinalResponse);
                        chatMemoryService.storeUserChat(sessionId, "assistant", ipFinalResponse);
                    }else{
                        result.put("fullResponse",ipFullResponse);
                        chatMemoryService.storeUserChat(sessionId, "assistant", ipFullResponse);
                    }

                    //TODO:Add speech
                    String ipOriginalSpeech = llmService.audioSmoothTranslation(result.get("fullResponse").toString(), model);
                    String ipSpeech = viancontroller.sendTextVIAN(ipOriginalSpeech);
                    result.put("audio", ipSpeech);

                    break;
                case "debt":
                    String dFinalInvoice = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_INVOICE));
                    String dFinalCustomerInfo = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_CUSTOMER_INFO));
                    String dFinalProductDetail = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_PRODUCT_INFO));
                    String dFinalInvoiceID = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_INVOICE_ID));
                    String dCombinedPrompt = "\nĐây là thông tin đầy đủ về sản phẩm: \n" +
                            dFinalProductDetail +
                            "\nĐây là thông tin ngày tháng năm hiện tại theo định dạng YYYY-MM-DD: \n" +
                            currentDate +
                            "\nĐây là thông tin hóa đơn: \n" +
                            dFinalInvoice +
                            "\nĐây là thông tin khách hàng: \n" +
                            dFinalCustomerInfo +
                            "\nĐây là thông tin invoice id (lấy mỗi số): \n" +
                            dFinalInvoiceID +
                            "\nĐây là thông tin hội thoại giữa user và assistant \n" +
                            lastHistory;
                    Map<String, String> debtResult = llmService.createDebtPayment(dCombinedPrompt, model);
                    String dSQLcommand = debtResult.get("sqlQuery");
                    String dFullResponse = debtResult.get("fullResponse");
                    if(!dSQLcommand.isEmpty()){
                        Integer executeCheck = sqlExecutionService.executeUpdate(dSQLcommand,dbParams);
                        String dFinalResponse = llmService.processAnalysis(dFullResponse,model);
                        if(executeCheck == 1){
                            //TODO:update invoice to Deffered
                            String dUpdateInvoiceSQL = llmService.updateDeferredInvoiceSQL(dFinalInvoiceID,model);
                            sqlExecutionService.executeUpdate(dUpdateInvoiceSQL,dbParams);
                            //TODO: generate invoice detail
                            String d_executeInvoiceDetailSQL = llmService.createInvoiceDetail(dCombinedPrompt,model);
                            sqlExecutionService.executeUpdate(d_executeInvoiceDetailSQL,dbParams);
                            //TODO:update stock
                            String dFinalProductInfo = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_PRODUCT_INFO));
                            String dUpdateStock = "\nThis is invoice information: \n" +
                                    dFinalInvoice +
                                    "\nThis is product information(take the number): \n" +
                                    dFinalProductInfo;
                            String sqlUpdateStock = llmService.updateStock(dUpdateStock,model);
                            sqlExecutionService.executeUpdate(sqlUpdateStock,dbParams);
                            //TODO: update customer last shopping date
                            String dCustomerInfo = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_CUSTOMER_INFO));
                            String dUpdateCustomerLastShoppingSQL = llmService.updateLastShopping(dCustomerInfo,model);
                            sqlExecutionService.executeQuery(dUpdateCustomerLastShoppingSQL,dbParams);

                            //TODO: add json
                            String dPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON).toString();
                            String dJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                    + dPrevJson
                                    + "\n Đây là thông tin hạn thanh toán: \n"+
                                    dFullResponse;
                            String dJsonString = llmService.jsonConverter(dJsonCombined,model);
                            JsonNode djsonNode = objectMapper.readTree(dJsonString);
                            chatMemoryService.storeEntityData(sessionId,FINAL_JSON,djsonNode.toString());
                            result.put("billInfo",djsonNode);

                        }
                        result.put("fullResponse",dFinalResponse);
                        chatMemoryService.storeUserChat(sessionId, "assistant", dFinalResponse);
                        //TODO:Add speech
                        String dOriginalSpeech = llmService.audioSmoothTranslation(dFinalResponse, model);
                        String dSpeech = viancontroller.sendTextVIAN(dOriginalSpeech);
                        result.put("audio", dSpeech);
                    }else{
                        result.put("fullResponse",dFullResponse);
                        chatMemoryService.storeUserChat(sessionId, "assistant", dFullResponse);
                        //TODO:Add speech
                        String dOriginalSpeech = llmService.audioSmoothTranslation(dFullResponse, model);
                        String dSpeech = viancontroller.sendTextVIAN(dOriginalSpeech);
                        result.put("audio", dSpeech);
                    }


                    break;
                case "check_debt":
                    //TODO: add default response if data is empty
                    //TODO: add user request to processCheck

                    String cdSQL = llmService.checkDebt(message,model);
                    String cdResultSQL = sqlExecutionService.executeQuery(cdSQL,dbParams).toString();
                    chatMemoryService.storeEntityData(sessionId,FINAL_CHECK_DEBT_INFO,cdResultSQL);
                    String cdFullResponse = llmService.processCheckDebt(cdResultSQL,model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", cdFullResponse);
                    result.put("fullResponse",cdFullResponse);
                    //TODO:Add speech
                    String cdOriginalSpeech = llmService.audioSmoothTranslation(cdFullResponse, model);
                    String cdSpeech = viancontroller.sendTextVIAN(cdOriginalSpeech);
                    result.put("audio", cdSpeech);

                    String cdPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON_DEBT).toString();
                    if(cdPrevJson.equalsIgnoreCase("[]")){
                        String cdJsonString = llmService.debtJsonConverter(cdResultSQL,model);
                        JsonNode cdjsonNode = objectMapper.readTree(cdJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_DEBT,cdjsonNode.toString());
                        result.put("debtInfo",cdjsonNode);
                    }else{
                        String cdJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                + cdPrevJson
                                + "\n Đây là thông tin hiện tại: \n"+
                                cdResultSQL;
                        String cdJsonString = llmService.debtJsonConverter(cdJsonCombined,model);
                        JsonNode cdjsonNode = objectMapper.readTree(cdJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_DEBT,cdjsonNode.toString());
                        result.put("debtInfo",cdjsonNode);
                    }

                    break;

                case "import":

                    String iCombinedPrompt = "\nThis is product stock information: \n" + fullDetailProductResult + "\nThis is user conversation: \n"+ lastHistory;
                    Map<String, String> importResponse = llmService.processImport(iCombinedPrompt, model);
                    //init json
                    //TODO: add json
                    String imPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON_IMPORT).toString();
                    if(imPrevJson.equalsIgnoreCase("[]")){
                        String imJsonString = llmService.importJsonConverter(importResponse.get("fullResponse"),model);
                        JsonNode imjsonNode = objectMapper.readTree(imJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_IMPORT,imjsonNode.toString());result.put("productInfo",imjsonNode);
                    }else{
                        String imJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                + imPrevJson
                                + "\n Đây là thông tin nhập kho: \n"+
                                lastHistory;
                        String imJsonString = llmService.jsonConverter(imJsonCombined,model);
                        JsonNode imjsonNode = objectMapper.readTree(imJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON,imjsonNode.toString());
                        result.put("productInfo",imjsonNode);
                    }

                    String iSQLcommand = importResponse.get("sqlQuery");
                    String iFullResponse = importResponse.get("fullResponse");
                    if(!iSQLcommand.isEmpty()){
                        sqlExecutionService.executeUpdate(iSQLcommand,dbParams);
                        String iFinalResponse = llmService.processAnalysis(iFullResponse,model);
                        result.put("fullResponse",iFinalResponse);
                        chatMemoryService.storeUserChat(sessionId, "assistant", iFinalResponse);

                        //TODO:Add speech
                        String imOriginalSpeech = llmService.audioSmoothTranslation(iFinalResponse, model);
                        String imSpeech = viancontroller.sendTextVIAN(imOriginalSpeech);
                        result.put("audio", imSpeech);

                        //

                    }else{
                        result.put("fullResponse",iFullResponse);
                        chatMemoryService.storeUserChat(sessionId, "assistant", iFullResponse);

                        //TODO:Add speech
                        String imOriginalSpeech = llmService.audioSmoothTranslation(iFullResponse, model);
                        String imSpeech = viancontroller.sendTextVIAN(imOriginalSpeech);
                        result.put("audio", imSpeech);
                    }

                    break;

                case "adjust_price":
                    String apCombinedPrompt = "\nThis is product stock information: \n" + fullDetailProductResult + "\nThis is user conversation: \n"+ lastHistory;
                    Map<String, String> apResponse = llmService.adjustProduct(apCombinedPrompt, model);
                    String apSQLcommand = apResponse.get("sqlQuery");
                    String apFullResponse = apResponse.get("fullResponse");
                    if(!apSQLcommand.isEmpty()){
                        sqlExecutionService.executeUpdate(apSQLcommand,dbParams);
                        String apFinalResponse = llmService.processAnalysis(apFullResponse,model);
                        result.put("fullResponse",apFinalResponse);
                        chatMemoryService.storeUserChat(sessionId, "assistant", apFinalResponse);
                    }else{
                        result.put("fullResponse",apFullResponse);
                        chatMemoryService.storeUserChat(sessionId, "assistant", apFullResponse);
                    }

                    //TODO: add JSON extracted information
                    String apPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON_ADJUST).toString();
                    if(apPrevJson.equalsIgnoreCase("[]")){
                        String apJsonString = llmService.importJsonConverter(apFullResponse,model);
                        JsonNode apjsonNode = objectMapper.readTree(apJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_ADJUST,apjsonNode.toString());
                        result.put("productAdjust",apjsonNode);
                    }else{
                        String apJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                + apPrevJson
                                + "\n Đây là thông tin hiện tại: \n"+
                                lastHistory;
                        String apJsonString = llmService.importJsonConverter(apJsonCombined,model);
                        JsonNode apjsonNode = objectMapper.readTree(apJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_ADJUST,apjsonNode.toString());
                        result.put("productAdjust",apjsonNode);
                    }

                    //TODO:Add speech
                    String apOriginalSpeech = llmService.audioSmoothTranslation(apFullResponse, model);
                    String apSpeech = viancontroller.sendTextVIAN(apOriginalSpeech);
                    result.put("audio", apSpeech);
                    break;

                case "restock_alert":
                    String callRestockAlert = " SELECT * FROM public.get_low_stock_products()";
                    String raResultSQL = sqlExecutionService.executeQuery(callRestockAlert,dbParams).toString();
                    String raFullResponse = llmService.stockAlert(raResultSQL,model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", raFullResponse);
                    result.put("fullResponse",raFullResponse);

                    //TODO:Add speech
                    String raOriginalSpeech = llmService.audioSmoothTranslation(raFullResponse, model);
                    String raSpeech = viancontroller.sendTextVIAN(raOriginalSpeech);
                    result.put("audio", raSpeech);

                    //TODO: add json
                    String raPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON_RESTOCK).toString();
                    if(raPrevJson.equalsIgnoreCase("[]")){
                        String raJsonString = llmService.restockJsonConverter(raFullResponse,model);
                        JsonNode rajsonNode = objectMapper.readTree(raJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_RESTOCK,rajsonNode.toString());
                        result.put("restockAlertInfo",rajsonNode);
                    }else{
                        String raJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                + raPrevJson
                                + "\n Đây là thông tin hiện tại: \n"+
                                raFullResponse;
                        String raJsonString = llmService.restockJsonConverter(raJsonCombined,model);
                        JsonNode rajsonNode = objectMapper.readTree(raJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON,rajsonNode.toString());
                        result.put("restockAlertInfo",rajsonNode);
                    }
                    break;

                case "business_analysis":
                    String baSQL = llmService.businessAnalysis("Hôm nay là ngày:" + currentDate + "\n" +message,model);
                    String baResultSQL = sqlExecutionService.executeQuery(baSQL,dbParams).toString();
                    String baCombinedPrompt =  "Hôm nay là ngày:" + currentDate
                            + "\n Dây là toàn bộ thông tin thõa mãn yêu cầu của người dùng \n" + baResultSQL
                            + "\n User Conversation \n" + message;

                    String baFullResponse = llmService.processBusinessAnalysis(baCombinedPrompt,model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", baFullResponse);
                    result.put("fullResponse",baFullResponse);

                    //TODO:Add speech
                    String baOriginalSpeech = llmService.audioSmoothTranslation(baFullResponse, model);
                    String baSpeech = viancontroller.sendTextVIAN(baOriginalSpeech);
                    result.put("audio", baSpeech);

                    //TODO: add json
                    String baPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON_BA).toString();
                    if(baPrevJson.equalsIgnoreCase("[]")){
                        String baJsonString = llmService.baJsonConverter(baFullResponse,model);
                        JsonNode bajsonNode = objectMapper.readTree(baJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_BA,bajsonNode.toString());
                        result.put("businessInfo",bajsonNode);
                    }else{
                        String baJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                + baPrevJson
                                + "\n Đây là thông tin hiện tại: \n"+
                                baFullResponse;
                        String baJsonString = llmService.baJsonConverter(baJsonCombined,model);
                        JsonNode baJsonNode = objectMapper.readTree(baJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_BA,baJsonNode.toString());
                        result.put("businessInfo",baJsonNode);
                    }
                    break;

                case "check_customer_inactive":
                    //TODO: add user request to processCheck
                    String ciSQL = llmService.checkCustomerShoppingActivity(message,model);
                    String ciResultSQL = sqlExecutionService.executeQuery(ciSQL,dbParams).toString();
                    String ciCombinedPrompt =  "Hôm nay là ngày tháng năm:" + currentDate
                            + "\n Dây là toàn bộ thông tin thõa mãn yêu cầu của người dùng \n" + ciResultSQL
                            + "\n User Conversation \n" + message;
                    String ciFullResponse = llmService.processCheckCustomerShoppingActivity(ciCombinedPrompt,model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", ciFullResponse);
                    result.put("fullResponse",ciFullResponse);

                    //TODO:Add speech
                    String ciOriginalSpeech = llmService.audioSmoothTranslation(ciFullResponse, model);
                    String ciSpeech = viancontroller.sendTextVIAN(ciOriginalSpeech);
                    result.put("audio", ciSpeech);

                    //TODO: add json
                    String ciPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON_INACTIVE).toString();
                    if(ciPrevJson.equalsIgnoreCase("[]")){
                        String ciJsonString = llmService.customerInactiveJsonConverter(ciFullResponse,model);
                        JsonNode cijsonNode = objectMapper.readTree(ciJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_INACTIVE,cijsonNode.toString());
                        result.put("inactiveInfo",cijsonNode);
                    }else{
                        String ciJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                + ciPrevJson
                                + "\n Đây là thông tin hiện tại: \n"+
                                ciFullResponse;
                        String ciJsonString = llmService.customerInactiveJsonConverter(ciJsonCombined,model);
                        JsonNode cijsonNode = objectMapper.readTree(ciJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON,cijsonNode.toString());
                        result.put("inactiveInfo",cijsonNode);
                    }
                    break;

                case "selling_trend":
                    String stSQL = llmService.productTrending("Hôm nay là ngày:" + currentDate + "\n" +message,model);
                    String stResultSQL = sqlExecutionService.executeQuery(stSQL,dbParams).toString();
                    String stCombinedPrompt =  "Hôm nay là ngày:" + currentDate
                            + "\n Dây là toàn bộ thông tin thỏa mãn yêu cầu của người dùng \n" + stResultSQL
                            + "\n User Conversation \n" + message;
                    String stFullResponse = llmService.processProductTrending(stCombinedPrompt,model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", stFullResponse);
                    result.put("fullResponse",stFullResponse);

                    //TODO:Add speech
                    String stOriginalSpeech = llmService.audioSmoothTranslation(stFullResponse, model);
                    String stSpeech = viancontroller.sendTextVIAN(stOriginalSpeech);
                    result.put("audio", stSpeech);

                    //TODO: add json
                    String stPrevJson = chatMemoryService.getEntityData(sessionId,FINAL_JSON_SELLING_TREND).toString();
                    if(stPrevJson.equalsIgnoreCase("[]")){
                        String stJsonString = llmService.sellingTrendJsonConverter(stFullResponse,model);
                        JsonNode stjsonNode = objectMapper.readTree(stJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_SELLING_TREND,stjsonNode.toString());
                        result.put("trendInfo",stjsonNode);
                    }else{
                        String stJsonCombined = "\n Đây là dữ liệu JSON hiện tại: \n"
                                + stPrevJson
                                + "\n Đây là thông tin hiện tại: \n"+
                                stFullResponse;
                        String stJsonString = llmService.sellingTrendJsonConverter(stJsonCombined,model);
                        JsonNode stjsonNode = objectMapper.readTree(stJsonString);
                        chatMemoryService.storeEntityData(sessionId,FINAL_JSON_SELLING_TREND,stjsonNode.toString());
                        result.put("trendInfo",stjsonNode);
                    }
                    break;
                default:
                    //TODO: add non related request response
                    System.out.println("Unknown response, no state change.");
                    return null;
            }




        } catch (Exception e) {
            logger.error("Error processing chat", e);
            result.put("error", "Failed to process the request: " + e.getMessage());
        }



        return result;
    }

}
