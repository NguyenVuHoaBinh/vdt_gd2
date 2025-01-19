package Viettel.backend.service;

import Viettel.backend.dto.ChatRequestDTO;
import Viettel.backend.service.chatmemory.ChatMemoryService;
import Viettel.backend.service.llm.LLMService;
import Viettel.backend.service.llm.LLMServiceFactory;
import Viettel.backend.service.rag.text2embed.OpenAiEmbeddingService;
import Viettel.backend.service.rag.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// TODO
//  log @slf4j
//  chat memory re-impl
//  deal with hard-coded stuff
//  prompt builder/ storage
@Service
public class MydioService {
    @Autowired
    private ChatMemoryService chatMemoryService;

    @Autowired
    private LLMServiceFactory llmServiceFactory;

    @Autowired
    private OpenAiEmbeddingService openAiEmbeddingService;

    @Autowired
    private SearchService searchService;

    // message limit in recent chat from chat memory
    private int fetchLimit = 5;

    // Part of redis key to define last book searched/ played/ etc
    // TODO: Unused LAST_PLAYED
    String LAST_SEARCHED = "lastSearched";
    String LAST_OPENED = "lastOpened";
    String LAST_PLAYED = "lastPlayed";

    Map<String, Function<ChatRequestDTO, String>> functions;

    public MydioService() {
        this.functions = new HashMap<>();

        functions.put("FIND", this::find);
        functions.put("EXECUTE", this::execute);
        functions.put("OPEN", this::open);
        functions.put("CLOSE", this::close);
    }

    public String getResponse(ChatRequestDTO userChat, String userIntent) {
        return functions.get(userIntent).apply(userChat);
    }

    public String analyzeUserIntent(ChatRequestDTO userChat) {
        String sessionId = userChat.getSessionId();

        // TODO: Unused playState
        List<String> searchState = chatMemoryService.fetchEntityData(sessionId, LAST_SEARCHED);
        List<String> openState = chatMemoryService.fetchEntityData(sessionId, LAST_OPENED);
        List<String> playState = chatMemoryService.fetchEntityData(sessionId, LAST_PLAYED);

        // No recent chat from ChatMemory
        if (searchState == null && openState == null) return "FIND";

        LLMService llmService = llmServiceFactory.createLLMService(userChat.getLlmModel());

        // TODO build a class for prompt retrieval
        String systemPrompt = "src/main/resources/static/mydio/analysis.txt";
        String userInput = chatMemoryService.fetchMostRecentChat(userChat);
        int maxTokens = 1000;
        double temperature = 0.1;

        return llmService.sendPrompt(systemPrompt, userInput, maxTokens, temperature);
    }

    public String find(ChatRequestDTO userChat) {
        LLMService llmService = llmServiceFactory.createLLMService(userChat.getLlmModel());

        // Step 1: Generate refined query and HyDE document
        String refinedQuery = llmService.generateRefinedQuery(userChat.getMessage());

        // Step 2: Generate embedding for the hypothetical document
        float[] hydeEmbedding = openAiEmbeddingService.embedText(refinedQuery);

        // Step 3: Perform hybrid search
        int numCandidates = 100;
        int numResults = 10;

        List<Map<String, Object>> searchResults = searchService.hybridSearch(
                    userChat.getMydioIndex(), refinedQuery, hydeEmbedding, numCandidates, numResults);

        // Step 4: Collect context from search results
        String context = collectContext(searchResults);
        String combinedPrompt = generateCombinedPrompt(userChat, context);

        // Step 5: Get LLM response
        // TODO system prompt
        String systemPrompt = "src/main/resources/static/mydio/find.txt";
        int maxTokens = 1000;
        double temperature = 0.1;

        String response = llmService.sendPrompt(
                systemPrompt, combinedPrompt, maxTokens, temperature);

        // Step 6: Update ChatMemory
        String sessionId = userChat.getSessionId();
        // TODO entity data
        chatMemoryService.storeEntityData(sessionId, LAST_OPENED, "");
        chatMemoryService.storeEntityData(sessionId, LAST_SEARCHED, response);
        chatMemoryService.storeUserChat(sessionId, "assistant", response);

        return response;
    }

    public String execute(ChatRequestDTO userChat) {
        LLMService llmService = llmServiceFactory.createLLMService(userChat.getLlmModel());
        List<String> openState = chatMemoryService.fetchEntityData(userChat.getSessionId(), LAST_OPENED);

        // TODO prompt retrieval
        String systemPrompt = "src/main/reousrces/static/mydio/execute.txt";
        String userInput = "\n Đây là dữ liệu sách đang phát hiện tại: \n" +
                openState +
                "\n Đây là thông tin hội thoại: \n" +
                chatMemoryService.fetchMostRecentChat(userChat);
        int maxTokens = 1000;
        double temperature = 0.1;

        // Step: get LLM response
        String response = llmService.sendPrompt(
                systemPrompt, userInput, maxTokens, temperature);

        // Step: update ChatMemory
        String sessionId = userChat.getSessionId();
        chatMemoryService.storeUserChat(sessionId, "assistant", response);

        return response;
    }

    public String open(ChatRequestDTO userChat) {
        LLMService llmService = llmServiceFactory.createLLMService(userChat.getLlmModel());
        List<String> openState = chatMemoryService.fetchEntityData(userChat.getSessionId(), LAST_OPENED);

        // TODO prompt builder/ retrieval
        String systemPrompt = "src/main/resources/static/mydio/start.txt";
        String userInput = "\n Đây là dữ liệu sách đang phát hiện tại: \n" +
                openState +
                "\n Đây là thông tin hội thoại: \n" +
                chatMemoryService.fetchMostRecentChat(userChat);

        int maxTokens = 1000;
        double temperature = 0.1;

        // Step: Get LLM response
        String response = llmService.sendPrompt(
                systemPrompt, userInput, maxTokens, temperature);

        // Step: update chat memory
        String sessionId = userChat.getSessionId();
        chatMemoryService.storeUserChat(sessionId, "assistant", response);
        chatMemoryService.storeEntityData(sessionId, LAST_OPENED, response);
        chatMemoryService.storeEntityData(sessionId, LAST_SEARCHED, "");

        return response;
    }

    public String close(ChatRequestDTO userChat) {
        String response = "Sách đã đóng, bạn có muốn nghe thêm sách nào nữa không?";
        String sessionId = userChat.getSessionId();

        chatMemoryService.storeUserChat(sessionId, "assistant", response);
        chatMemoryService.storeEntityData(sessionId, LAST_OPENED, "");
        chatMemoryService.storeEntityData(sessionId, LAST_SEARCHED, "");

        return response;
    }

    public String collectContext(List<Map<String, Object>> searchResults) {
        // TODO: generalize for multiple schemas
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
        return contextBuilder.toString();
    }

    public String generateCombinedPrompt(ChatRequestDTO userChat, String context) {
        // Get recent chats (Last n messages) & new message
        String recentChats = chatMemoryService.fetchRecentChats(userChat, fetchLimit);

        // TODO prompt builder
        String combinedPrompt = userChat.getRole() +
                        "\n\nBOOK INFORMATION :\n" + context +
                        "\n\nConversation History:\n" + recentChats;

        return combinedPrompt;
    }
}
