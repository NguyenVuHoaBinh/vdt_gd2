package Viettel.backend.service.llmservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class LLMService {
    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);

    private final SQLOpenAiService sqlOpenAiService;
    private final SQLGeminiService sqlGeminiService;

    @Autowired
    public LLMService(SQLOpenAiService sqlOpenAiService, SQLGeminiService sqlGeminiService) {
        this.sqlOpenAiService = sqlOpenAiService;
        this.sqlGeminiService = sqlGeminiService;
    }

    public Map<String, String> processMessage(String message, String model, String role) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processMessage(message, role);
            case "gemini":
                return sqlGeminiService.processMessage(message, role);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String processAnalysis(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processAnalysis(message);
            case "gemini":
                return sqlGeminiService.processAnalysis(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String generateRefinedQuery(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.generateRefinedQuery(message);
            case "gemini":
                return sqlGeminiService.generateRefinedQuery(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String generateHyDE(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.generateHyDE(message);
            case "gemini":
                return sqlGeminiService.generateHyDE(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String errorSolver(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.errorSolver(message);
            case "gemini":
                return sqlGeminiService.errorSolver(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
}
