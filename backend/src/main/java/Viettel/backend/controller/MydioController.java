package Viettel.backend.controller;

import Viettel.backend.dto.ChatRequestDTO;
import Viettel.backend.dto.ChatResponseDTO;
import Viettel.backend.model.ChatDocument;
import Viettel.backend.service.MydioService;
import Viettel.backend.service.chatmemory.RedisChatMemoryService;
import Viettel.backend.service.rag.text2embed.EmbeddingService;
import Viettel.backend.service.rag.text2embed.EmbeddingServiceFactory;
import Viettel.backend.service.tts.ViAnService;
import jakarta.validation.Valid;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.search.Document;

import java.util.List;

@RestController
@RequestMapping("/v2/mydio")
public class MydioController {
    @Autowired
    private ViAnService viAnService;

    @Autowired
    private MydioService mydioService;

    @Autowired
    RedisChatMemoryService redisChatMemoryService;

    @Autowired
    EmbeddingServiceFactory embeddingServiceFactory;

    // TODO toberemoved: this is for testing
    @GetMapping("/b")
    public ResponseEntity<String> demoEmbedding() {
        EmbeddingService embeddingService = embeddingServiceFactory.createEmbeddingService("x");
        System.out.println(embeddingService.floatArrayToByteArray(embeddingService.embedText("hello")) instanceof byte[]);

        return ResponseEntity.ok("Hello World");
    }

    // TODO toberemoved: this is for testing
    @GetMapping("/a")
    public ResponseEntity<String> demoChatMemory1() {
        redisChatMemoryService.createIndex();
        EmbeddingService embeddingService = embeddingServiceFactory.createEmbeddingService("x");

        List<ChatDocument> chatDocuments = List.of(
                new ChatDocument("Hello, how can I help you today?", "Hi, I want to check the status of my order."),
                new ChatDocument("Can you provide your order ID?", "Sure, it’s 123456."),
                new ChatDocument("Thank you. Your order is currently being processed.", "When will it be delivered?"),
                new ChatDocument("It should arrive by tomorrow evening.", "Great, thank you."),
                new ChatDocument("You're welcome! Is there anything else I can help with?", "No, that's all for now."),
                new ChatDocument("Hi again! I need help with another order.", "Of course! What's the order ID?"),
                new ChatDocument("It's 654321. Can you check its status?", "Your order has been shipped and will arrive in 3 days."),
                new ChatDocument("Thanks for the update. Can I change the delivery address?", "Yes, please provide the new address."),
                new ChatDocument("The new address is 456 Elm Street.", "Got it. The address has been updated successfully."),
                new ChatDocument("Appreciate the help!", "Always happy to assist!")
//                new ChatDocument("That is a very happy person"),
//                new ChatDocument("That is a happy dog"),
//                new ChatDocument("Today is a sunny day")
        );

        chatDocuments.forEach(doc -> {
            doc.setEmbedding(embeddingService.floatArrayToByteArray(embeddingService.embedText(doc.getText())));
//            System.out.println(doc.getEmbedding().length/4);
        });
        redisChatMemoryService.indexDocuments(chatDocuments);

//        byte[] userMessage = embeddingService.floatArrayToByteArray(embeddingService.embedText("That is a happy person"));
        byte[] userMessage = embeddingService.floatArrayToByteArray(embeddingService.embedText("Do you remember my order id?"));
        System.out.println("Query vector size: " + userMessage.length / 4); // Divide by 4 for float32
//
        for (Document document :redisChatMemoryService.searchDocuments(userMessage, "123123132", 5)) {
            System.out.println("=============================\\n");

            System.out.println(document.toString());
        }
        return ResponseEntity.ok("Hello World");
    }

    // TODO toberemoved: this is for testing
    @GetMapping("/c")
    public ResponseEntity<String> demoChatMemory2() {


        System.out.println(redisChatMemoryService.getAllSessionIds());
        redisChatMemoryService.deleteDocuments("123123132");
        System.out.println(redisChatMemoryService.getAllSessionIds());

        return ResponseEntity.ok("Hello World");

    }

    @PostMapping("/greet")
    public ChatResponseDTO greeting(){
        // TODO fe not displaying greetings at the beginning (its showing under inspect tho)
        String response = "Chào bạn, đây là ứng dụng sách nói Mydio của Viettel. Hôm nay bạn muốn nghe sách gì?";
        String tts = "Chào bạn, đây là ứng dụng sách nói mai đi ô của việt ten. Hôm nay bạn muốn nghe sách gì?";
        String audio = viAnService.encodeAudioToBase64(tts);

        return new ChatResponseDTO(response, audio);
    }

    @PostMapping("/chat")
    public ChatResponseDTO chat(@Valid @RequestBody ChatRequestDTO userChat) {
        // Define user intent: greeting/ find/ execute/ open/ close
        String userIntent = mydioService.analyzeUserIntent(userChat);
        if (userIntent.equals("GREETING")) return greeting();

        String response = mydioService.getResponse(userChat, userIntent);
        String audio = viAnService.encodeAudioToBase64(response);

        return new ChatResponseDTO(response, audio);
    }

}

