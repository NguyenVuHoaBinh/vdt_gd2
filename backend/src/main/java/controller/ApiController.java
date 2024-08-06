package controller;

import model.DBParams;
import model.ChatRequest;
import service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private DBService dbService;

    @PostMapping("/connect")
    public ResponseEntity<?> connectToDB(@Validated @RequestBody DBParams dbParams) {
        boolean success = dbService.connectToDB(dbParams);
        return ResponseEntity.ok().body("{\"success\":" + success + "}");
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Validated @RequestBody ChatRequest chatRequest) {
        String reply = dbService.chat(chatRequest);
        return ResponseEntity.ok().body("{\"reply\":\"" + reply + "\"}");
    }
}
