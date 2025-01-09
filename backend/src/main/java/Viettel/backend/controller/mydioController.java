package Viettel.backend.controller;

import Viettel.backend.AdvanceRAG.service.OpenAiEmbeddingService;
import Viettel.backend.AdvanceRAG.service.SearchService;
import Viettel.backend.dto.ChatRequestDTO;
import Viettel.backend.dto.ChatResponseDTO;
import Viettel.backend.service.MydioService;
import Viettel.backend.service.UserChatService;
import Viettel.backend.service.ViAnService;
import Viettel.backend.service.ChatMemoryService;
import Viettel.backend.service.llmservice.LLMService;
import Viettel.backend.service.llmservice.LLMServiceFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v2/mydio")
public class MydioController {


    @Autowired
    private ViAnService viAnService;

    @Autowired
    private OpenAiEmbeddingService openAiEmbeddingService;

    @Autowired
    private ChatMemoryService chatMemoryService;

    @Autowired
    private MydioService mydioService;

    @Autowired
    private SearchService searchService;
    
    @PostMapping("/greeting")
    public ChatResponseDTO greeting(){
        String greetingResponse = "Chào bạn, đây là ứng dụng sách nói Mydio của Viettel. Hôm nay bạn muốn nghe sách gì?";
            String greetingAudio = "Chào bạn, đây là ứng dụng sách nói mai đi ô của việt ten. Hôm nay bạn muốn nghe sách gì?";
            String greetingSpeech = viAnService.encodeAudioToBase64(greetingAudio);

            return new ChatResponseDTO(greetingResponse, greetingSpeech);
        }

        @PostMapping("/chat")
        public ChatResponseDTO chat(@Valid @RequestBody ChatRequestDTO userChat) {
            // TODO: unknown system role
            String message = userChat.getMessage();
            String model = userChat.getModel();
            String systemRole = ""; // userChat.getSystemRole();
            String sessionId = userChat.getSessionId();

            // TODO: extend Mydio with not just books
            String MYDIO_INDEX = "books";

            // Part of redis key to define last book searched/ played etc
            // TODO: LAST_PLAYED isn't used
            String LAST_SEARCHED = "lastSearched";  // find | open | close
            String LAST_OPENED = "lastOpened";      // open | close
            String LAST_PLAYED = "lastPlayed";

            List<String> stateSearch = chatMemoryService.fetchEntityData(sessionId, LAST_SEARCHED);
            List<String> stateBook = chatMemoryService.fetchEntityData(sessionId, LAST_OPENED);
            List<String> statePlaying = chatMemoryService.fetchEntityData(sessionId, LAST_PLAYED);
            try {


            // Define user intent: greeting | find | execute | open | close
            String userIntent = mydioService.analyzeUserIntent(userChat);

            // Get the most recent searched/ opened/ closed/ played book
            // TODO: refactor


            if(stateSearch == null && stateBook == null) userIntent = "find";

            // TODO: design pattern
            switch (userIntent) {
                case "GREETING":
                    return greeting();
                case "FIND":
                    mydioService.find(userChat, MYDIO_INDEX);


                    chatMemoryService.storeEntityData(sessionId, LAST_OPENED, "");


                    //modify
                    String fullResponse = llmService.mydioCall(combinedPrompt, model);
                    chatMemoryService.storeUserChat(sessionId, "assistant", fullResponse);
                    String fSpeech = viancontroller.sendTextVIAN(fullResponse);
                    result.put("fullResponse", fullResponse);
                    result.put("audio", fSpeech);
                    chatMemoryService.storeEntityData(sessionId,LAST_SEARCHED,fullResponse);
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
                    chatMemoryService.storeEntityData(sessionId, LAST_OPENED, oResponse);
                    chatMemoryService.storeEntityData(sessionId, LAST_SEARCHED, "");
                    break;
                case "close":
                    String cResponse = "Sách đã đóng, bạn có muốn nghe thêm sách nào nữa không?";
                    chatMemoryService.storeUserChat(sessionId, "assistant", cResponse);
                    String cSpeech = viancontroller.sendTextVIAN(cResponse);
                    result.put("fullResponse", cResponse);
                    result.put("audio", cSpeech);
                    chatMemoryService.storeEntityData(sessionId, LAST_OPENED, "");
                    chatMemoryService.storeEntityData(sessionId, LAST_SEARCHED, "");
                    break;
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}

