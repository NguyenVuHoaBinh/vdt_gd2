package Viettel.backend.controller;


import Viettel.backend.config.DatabaseConfig;
import Viettel.backend.service.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin("*")
//@RequestMapping("/clients")`
public class ChatController {

    @Autowired
    private DatabaseConfig databaseConfig;

    @Autowired
    private LLMService llmService;

    private JdbcTemplate jdbcTemplate;

    @PostMapping("/connect")
    public Map<String, Object> connect(@RequestBody Map<String, String> dbParams) {
        Map<String, Object> response = new HashMap<>();
        try {
            DataSource dataSource = databaseConfig.createDataSource(dbParams);
            this.jdbcTemplate = new JdbcTemplate(dataSource);
            // Test the connection
            jdbcTemplate.execute("SELECT 1");
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> chatParams) {
        String message = chatParams.get("message");
        String model = chatParams.get("model");
        String response = llmService.processMessage(message, model);
        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return result;
    }

    @PostMapping("/upload")
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file) {
        // Handle file upload logic here
        Map<String, String> response = new HashMap<>();
        response.put("response", "File uploaded successfully: " + file.getOriginalFilename());
        return response;
    }
}
