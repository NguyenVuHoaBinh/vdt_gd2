package Viettel.backend.service.llmservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SQLGeminiService implements LLMServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(SQLGeminiService.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;

    public SQLGeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<String, String> processMessage(String message, String role) {
        String url = "https://api.gemini.com/v1/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(geminiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", message);
        requestBody.put("max_tokens", 100);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                String fullResponse = (String) choices.get(0).get("text");

                String sqlQuery = extractSQLQuery(fullResponse);

                Map<String, String> result = new HashMap<>();
                result.put("fullResponse", fullResponse);
                result.put("sqlQuery", sqlQuery);
                return result;
            }
        } else {
            throw new RuntimeException("Failed to get response from Gemini");
        }
        return null;
    }

    @Override
    public String processAnalysis(String message) {
        return "";
    }

    @Override
    public String generateHyDE(String message) {
        return "";
    }

    @Override
    public String generateRefinedQuery(String message) {
        return "";
    }

    @Override
    public String errorSolver(String message) {
        return "";
    }

    @Override
    public String create_invoice(String message) {
        return "";
    }

    @Override
    public String create_invoice_warehouse(String message) {
        return "";
    }

    @Override
    public String get_product_detail(String message) {
        return "";
    }

    @Override
    public String task_analysis(String message) {
        return "";
    }

    @Override
    public String processOrder(String message) {
        return "";
    }

    @Override
    public String checkCustomer(String message) {
        return "";
    }

    @Override
    public Map<String, String> createCustomerSQL(String message) {
        return null;
    }

    @Override
    public String isOrder(String message) {
        return "";
    }

    @Override
    public String isCustomerInfo(String message) {
        return "";
    }

    @Override
    public String isInvoice(String message) {
        return "";
    }

    @Override
    public String isContainSQL(String message) {
        return "";
    }

    @Override
    public String processPayment(String message) {
        return "";
    }

    @Override
    public Map<String, String> createImmediatelyPayment(String message) {
        return Map.of();
    }

    @Override
    public String createInvoiceSQL(String message) {
        return "";
    }

    @Override
    public String updatePaidInvoiceSQL(String message) {
        return "";
    }

    @Override
    public String updateDeferredInvoiceSQL(String message) {
        return "";
    }

    @Override
    public String updateCancelledInvoiceSQL(String message) {
        return "";
    }

    @Override
    public String updateInvoiceSQL(String message) {
        return "";
    }

    @Override
    public String createInvoiceDetail(String message) {
        return "";
    }

    @Override
    public String updateStock(String message) {
        return "";
    }

    @Override
    public Map<String, String> createDebtPayment(String message) {
        return Map.of();
    }

    @Override
    public String checkDebt(String message) {
        return "";
    }

    @Override
    public String processCheckDebt(String message) {
        return "";
    }

    @Override
    public Map<String, String> processImport(String message) {
        return Map.of();
    }

    @Override
    public Map<String, String> adjustProduct(String message) {
        return Map.of();
    }

    @Override
    public String stockAlert(String message) {
        return "";
    }

    @Override
    public String updateLastShopping(String message) {
        return "";
    }

    @Override
    public String checkCustomerShoppingActivity(String message) {
        return "";
    }

    @Override
    public String processCheckCustomerShoppingActivity(String message) {
        return "";
    }

    @Override
    public String sellingTrend(String message) {
        return "";
    }

    @Override
    public String businessAnalysis(String message) {
        return "";
    }

    @Override
    public String processBusinessAnalysis(String message) {
        return "";
    }

    @Override
    public String productTrending(String message) {
        return "";
    }

    @Override
    public String processProductTrending(String message) {
        return "";
    }

    @Override
    public String audioSmoothTranslation(String message) {
        return "";
    }

    @Override
    public String jsonConverter(String message) {
        return "";
    }

    @Override
    public String importJsonConverter(String message) {
        return "";
    }

    @Override
    public String baJsonConverter(String message) {
        return "";
    }

    @Override
    public String debtJsonConverter(String message) {
        return "";
    }

    @Override
    public String customerInactiveJsonConverter(String message) {
        return "";
    }

    @Override
    public String restockJsonConverter(String message) {
        return "";
    }

    @Override
    public String sellingTrendJsonConverter(String message) {
        return "";
    }

    @Override
    public String mydioCall(String message) {
        return "";
    }

    @Override
    public String mydioExec(String message) {
        return "";
    }

    @Override
    public String mydioAnalysis(String message) {
        return "";
    }

    @Override
    public String mydioStart(String message) {
        return "";
    }

    @Override
    public String ambiguousDetection(String message) {
        return "";
    }


    private String extractSQLQuery(String fullResponse) {
        // Similar extraction logic as OpenAiService
        return fullResponse;
    }
}
