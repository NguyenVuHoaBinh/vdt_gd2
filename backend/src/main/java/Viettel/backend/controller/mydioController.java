package Viettel.backend.controller;

import Viettel.backend.AdvanceRAG.service.Chunker;
import Viettel.backend.AdvanceRAG.service.OpenAIEmbeddingService;
import Viettel.backend.AdvanceRAG.service.SearchService;
import Viettel.backend.config.databaseconfig.DatabaseConfig;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")

public class mydioController {
    private static final Logger logger = LoggerFactory.getLogger(SQLChatController.class);

    @Autowired
    private viAnController viancontroller;
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


    private final Map<String, String> dbParamsStore = new HashMap<>();
    private JdbcTemplate jdbcTemplate;

    private String indexName = "books";
    @PostMapping("/v2/Mydio_greeting")
    public Map<String, Object> greeting(@RequestBody Map<String, Object> chatParams){
        Map<String, Object> result = new HashMap<>();
        String greetingResponse ="Chào bạn,đây là ứng dụng sách nói Mydio của Viettel. Hôm nay bạn muốn nghe sách gì?";
        String greetingAudio = "Chào bạn,đây là ứng dụng sách nói mai đi ô của việt ten. Hôm nay bạn muốn nghe sách gì?";
        String greetingSpeech = viancontroller.sendTextVIAN(greetingAudio);
        result.put("fullResponse", greetingResponse);
        result.put("audio", greetingSpeech);
        return result;
    }

    @PostMapping("/v2/Mydio_chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> chatParams) {
        Map<String, Object> result = new HashMap<>();
        try {
            UUID uuid = UUID.randomUUID();

            // Convert UUID to string
            String randomUUIDString = uuid.toString();
            String message = (String) chatParams.get("message");
            String model = "gpt-3";
            String systemRole = "";
            String sessionId = (String) chatParams.get("sessionId");


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
            String FINAL_SEARCH = "finalSearch";
            String FINAL_BOOK = "finalBook";
            String FINAL_PLAYING = "finalPlaying";


//          Product database
            // Execute the SQL query and retrieve a list of result maps

            // Combine system role, schema metadata, and user message to create an enhanced prompt


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
            chatMemoryService.storeUserChat(sessionId, "user", message);

            String analysisResponse = llmService.mydioAnalysis(lastHistory,model);
            // Switch case here
            System.out.println(analysisResponse);
            String stateSearch = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_SEARCH));
            String stateBook = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_BOOK));
            String statePlaying = String.valueOf(chatMemoryService.getEntityData(sessionId,FINAL_PLAYING));
            if(stateSearch.equalsIgnoreCase("[]") && stateBook.equalsIgnoreCase("[]")){
                analysisResponse = "find";
            }
            switch (analysisResponse.toLowerCase()) {
                case "greeting":
                    String greetingResponse ="Chào bạn,đây là ứng dụng sách nói Mydio của Viettel. Hôm nay bạn muốn nghe sách gì?";
                    String greetingAudio = "Chào bạn,đây là ứng dụng sách nói mai đi ô của việt ten. Hôm nay bạn muốn nghe sách gì?";
                    String greetingSpeech = viancontroller.sendTextVIAN(greetingAudio);
                    result.put("fullResponse", greetingResponse);
                    result.put("audio", greetingSpeech);
                    break;
                case "find":
                    chatMemoryService.storeEntityData(sessionId, FINAL_BOOK, "");
                    // Step 1: Generate refined query and HyDE document
                    String refinedQuery = llmService.generateRefinedQuery(message, model);

                    // Step 2: Generate embedding for the hypothetical document
                    double[] hydeEmbedding = embeddingService.getEmbedding(refinedQuery);

                    // Step 3: Perform hybrid search
                    int numCandidates = 100;
                    int numResults = 10;

                    List<Map<String, Object>> searchResults = searchService.hybridSearch(
                            indexName, refinedQuery, hydeEmbedding, numCandidates, numResults);

                    // Step 4: Collect context from search results
                    StringBuilder contextBuilder = new StringBuilder();
                    int count = 1;
                    for (Map<String, Object> searchResult : searchResults) {
                        String id = (String) searchResult.get("id");
                        String bookName = (String) searchResult.get("name");
                        String authorName = (String) searchResult.get("authors_name");
                        String label = (String) searchResult.get("label");
                        Integer view = (Integer) searchResult.get("view");

                        if (bookName != null) {
                            contextBuilder.append(count++).append(".\n")
                                    .append("id: ").append(id != null ? id : "N/A").append("\n")
                                    .append("bookName: ").append(bookName != null ? bookName : "N/A").append("\n")
                                    .append("authorName: ").append(authorName != null ? authorName : "N/A").append("\n")
                                    .append("label: ").append(label != null ? label : "N/A").append("\n")
                                    .append("view: ").append(view != null ? view : "N/A").append("\n\n");
                        }
                    }

                    String output = contextBuilder.toString();

                    String context = contextBuilder.toString();

                    String combinedPrompt =
                            (systemRole != null ? systemRole : "") +
                                    "\n\nBOOK INFORMATION :\n" + context +
                                    "\n\nConversation History:\n" + conversationHistory;

                    logger.info("Enhanced Prompt: \n{}", combinedPrompt);

                    //modify
                    String fullResponse = llmService.mydioCall(combinedPrompt, model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", fullResponse);
                    String fSpeech = viancontroller.sendTextVIAN(fullResponse);
                    result.put("fullResponse", fullResponse);
                    result.put("audio", fSpeech);
                    chatMemoryService.storeEntityData(sessionId,FINAL_SEARCH,fullResponse);
                    break;
                case "execute":

                    String eCombined = "\n Đây là dữ liệu sách đang phát hiện tại: \n"
                            + stateBook
                            + "\n Đây là thông tin hội thoại: \n"+
                            lastHistory;
                    String eResponse = llmService.mydioExec(eCombined,model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", eResponse);
                    String eSpeech = viancontroller.sendTextVIAN(eResponse);
                    result.put("fullResponse", eResponse);
                    result.put("audio", eSpeech);
                    break;
                case "open":
                    String oCombined = "\n Đây là dữ liệu sách đang phát hiện tại: \n"
                            + stateBook
                            + "\n Đây là thông tin hội thoại: \n"+
                            lastHistory;
                    String oResponse = llmService.mydioStart(oCombined,model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", oResponse);
                    String oSpeech = viancontroller.sendTextVIAN(oResponse);
                    result.put("fullResponse", oResponse);
                    result.put("audio", oSpeech);
                    chatMemoryService.storeEntityData(sessionId, FINAL_BOOK, oResponse);
                    chatMemoryService.storeEntityData(sessionId, FINAL_SEARCH, "");
                    break;
                case "close":
                    String cResponse = "Sách đã đóng, bạn có muốn nghe thêm sách nào nữa không?";
                    chatMemoryService.storeUserChat(sessionId, "assistant", cResponse);
                    String cSpeech = viancontroller.sendTextVIAN(cResponse);
                    result.put("fullResponse", cResponse);
                    result.put("audio", cSpeech);
                    chatMemoryService.storeEntityData(sessionId, FINAL_BOOK, "");
                    chatMemoryService.storeEntityData(sessionId, FINAL_SEARCH, "");
                    break;
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    return result;
    }

}
