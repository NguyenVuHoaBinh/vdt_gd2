package Viettel.backend.service.llmservice;

import java.util.Map;

public interface LLMServiceInterface {
    Map<String, String> processMessage(String message, String role);
    String processAnalysis(String message);
    String generateHyDE(String message);
    String generateRefinedQuery(String message);
}
