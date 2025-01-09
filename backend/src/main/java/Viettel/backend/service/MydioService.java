package Viettel.backend.service;

import Viettel.backend.controller.SQLChatController;
import Viettel.backend.dto.ChatRequestDTO;
import Viettel.backend.service.llmservice.LLMService;
import Viettel.backend.service.llmservice.LLMServiceFactory;
import Viettel.backend.AdvanceRAG.service.OpenAiEmbeddingService;
import Viettel.backend.AdvanceRAG.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MydioService {
    private static final Logger logger = LoggerFactory.getLogger(SQLChatController.class);
    @Autowired
    private ChatMemoryService chatMemoryService;

    @Autowired
    private LLMServiceFactory llmServiceFactory;

    @Autowired
    private OpenAiEmbeddingService openAiEmbeddingService;

    @Autowired
    private SearchService searchService;

    public String processUserChat(ChatRequestDTO userChat) {
        String sessionId = userChat.getSessionId();
        String message = userChat.getMessage();

        // Get chat history of current session
        List<String> chatHistory = chatMemoryService.getUserChat(sessionId);

        // Fetch recent chats (Last n messages) & append new message
        int n = 5;
        String recentChats = chatMemoryService.fetchRecentChats(chatHistory, n, message);

        // Log recent chat
        logger.debug("Conversation History:\n{}", recentChats);

        // Fetch most recent chat (Last message) & append new message
        String latestChat = chatMemoryService.fetchRecentChats(chatHistory, 1, message);

        // Store new message into chat memory (Redis)
        // TODO: replace "user"
        chatMemoryService.storeUserChat(sessionId, "user", message);

        return latestChat;
    }

    public String analyzeUserIntent(ChatRequestDTO userChat) {
        String latestChat = processUserChat(userChat);
        LLMService llmService = llmServiceFactory.createLLMService(userChat.getModel());

        String systemPrompt = "src/main/resources/static/mydio/analysis.txt";

        // TODO strategy pattern for prompt filepath ?
        return llmService.sendPrompt(systemPrompt, latestChat, 1000, 0.1);
    }

    public void find(ChatRequestDTO userChat, String indexName) {
        // Step 1: Generate refined query and HyDE document
        String refinedQuery = generateRefinedQuery(userChat);

        // Step 2: Generate embedding for the hypothetical document
        double[] hydeEmbedding = openAiEmbeddingService.getEmbedding(refinedQuery);

        // Step 3: Perform hybrid search
        int numCandidates = 100;
        int numResults = 10;

        List<Map<String, Object>> searchResults;
        try {
            searchResults = searchService.hybridSearch(
                    indexName, refinedQuery, hydeEmbedding, numCandidates, numResults);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Step 4: Collect context from search results
        String context = collectContext(searchResults);
        String combinedPrompt = generateCombinedPrompt(userChat, context);



    }

    public String generateRefinedQuery(ChatRequestDTO userChat) {
        LLMService llmService = llmServiceFactory.createLLMService(userChat.getModel());
        String systemPrompt = "src/main/resources/static/refiningQuery.txt";

        // TODO strategy pattern for prompt filepath
        return llmService.sendPrompt(systemPrompt, userChat.getMessage(), 200, 1);
    }

    public

    public String collectContext(List<Map<String, Object>> searchResults) {
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
        // Get chat history of current session
        List<String> chatHistory = chatMemoryService.getUserChat(userChat.getSessionId());

        // Fetch recent chats (Last n messages) & append new message
        int n = 5;
        String recentChats = chatMemoryService.fetchRecentChats(chatHistory, n, userChat.getMessage());

        String systemRole = userChat.getSystemRole();
        String combinedPrompt = systemRole +
                        "\n\nBOOK INFORMATION :\n" + context +
                        "\n\nConversation History:\n" + recentChats;

        logger.info("Enhanced Prompt: \n{}", combinedPrompt);

        return


    }




//    public String mydioAnalysis(String message) {
//        String role = "";
//        try {
//            String filePath = "src/main/resources/static/mydio/analysis.txt"; // Update with the correct path to your file
//            role = new String(Files.readAllBytes(Paths.get(filePath)));
//            System.out.println(role);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String url = "https://api.openai.com/v1/chat/completions";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(openAiApiKey);
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
//        requestBody.put("messages", List.of(
//                Map.of("role", "system", "content", role),
//                Map.of("role", "user", "content", message)
//        ));
//        requestBody.put("max_tokens", 1000);
//        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
//        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
//            if (choices != null && !choices.isEmpty()) {
//                Map<String, Object> firstChoice = choices.get(0);
//                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
//                String fullResponse = (String) messageMap.get("content");
//
//                return fullResponse;
//            }
//        } else {
//            throw new RuntimeException("Failed to get response from OpenAI");
//        }
//        return null;
//    }
}
