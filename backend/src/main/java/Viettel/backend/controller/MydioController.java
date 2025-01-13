package Viettel.backend.controller;

import Viettel.backend.dto.ChatRequestDTO;
import Viettel.backend.dto.ChatResponseDTO;
import Viettel.backend.service.MydioService;
import Viettel.backend.service.ViAnService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/mydio")
public class MydioController {
    @Autowired
    private ViAnService viAnService;

    @Autowired
    private MydioService mydioService;

    @PostMapping("/greet")
    public ChatResponseDTO greeting(){
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

