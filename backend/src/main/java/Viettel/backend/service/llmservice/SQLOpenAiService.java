package Viettel.backend.service.llmservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SQLOpenAiService implements LLMServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(SQLOpenAiService.class);

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate;

    public SQLOpenAiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<String, String> processMessage(String message, String role) {

        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                Map<String, String> result = new HashMap<>();
                result.put("fullResponse", fullResponse);
                result.put("sqlQuery", sqlQuery);
                return result;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String processAnalysis(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/SQLSystemPrompt.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String generateHyDE(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/generateHYDE.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String generateRefinedQuery(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/refiningQuery.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String errorSolver(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/errorSolver.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String create_invoice(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/invoicePrompt.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String create_invoice_warehouse(String message) {
        return "";
    }

    @Override
    public String get_product_detail(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/getProductDetail.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String task_analysis(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/taskAnalysis.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String processOrder(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/processOrder.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");
                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String checkCustomer(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/checkCustomerSQL.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public Map<String, String> createCustomerSQL(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/createCustomerSQL.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                Map<String, String> result = new HashMap<>();
                result.put("fullResponse", fullResponse);
                result.put("sqlQuery", sqlQuery);
                return result;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String isOrder(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/isOrder.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String isCustomerInfo(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/isCustomer.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String isInvoice(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/isInvoice.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String isContainSQL(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/isInvoice.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String processPayment(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/processPayment.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 200);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String analysisResult = (String) messageMap.get("content");


                return analysisResult;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public Map<String, String> createImmediatelyPayment(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/createImmediatelyPayment.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");
                Map<String, String> result = new HashMap<>();
                String sqlQuery = extractSQLQuery(fullResponse);

                result.put("sqlQuery", sqlQuery);
                result.put("fullResponse", fullResponse);
                return result;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String createInvoiceSQL(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/createInvoiceSQL.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String updatePaidInvoiceSQL(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/updatePaidInvoiceSQL.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String updateDeferredInvoiceSQL(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/updateDeferredInvoiceSQL.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
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
        String role = "";
        try {
            String filePath = "src/main/resources/static/createInvoiceDetailSQL.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String updateStock(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/updateStock.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public Map<String, String> createDebtPayment(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/createDebtPayment.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");
                Map<String, String> result = new HashMap<>();
                String sqlQuery = extractSQLQuery(fullResponse);

                result.put("sqlQuery", sqlQuery);
                result.put("fullResponse", fullResponse);
                return result;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String checkDebt(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/checkDebt.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String processCheckDebt(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/processCheckDebt.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public Map<String, String> processImport(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/processImport.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");
                Map<String, String> result = new HashMap<>();
                String sqlQuery = extractSQLQuery(fullResponse);

                result.put("sqlQuery", sqlQuery);
                result.put("fullResponse", fullResponse);
                return result;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public Map<String, String> adjustProduct(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/adjustPrice.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");
                Map<String, String> result = new HashMap<>();
                String sqlQuery = extractSQLQuery(fullResponse);

                result.put("sqlQuery", sqlQuery);
                result.put("fullResponse", fullResponse);
                return result;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String stockAlert(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/stock_alert.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String updateLastShopping(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/updateLastShopping.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String checkCustomerShoppingActivity(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/checkCustomerShoppingActivity.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String processCheckCustomerShoppingActivity(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/processCheckCustomerShoppingActivity.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String sellingTrend(String message) {
        return "";
    }

    @Override
    public String businessAnalysis(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/businessAnalysis.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String processBusinessAnalysis(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/processCheckCustomerShoppingActivity.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String productTrending(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/productTrending.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                String sqlQuery = extractSQLQuery(fullResponse);

                return sqlQuery;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String processProductTrending(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/processProductTrending.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String audioSmoothTranslation(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/audioSmoothTranslation.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String jsonConverter(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/jsonConverter.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String importJsonConverter(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/importJsonConverter.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String baJsonConverter(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/baJsonConverter.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 600);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String debtJsonConverter(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/debtJsonConverter.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 1000);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String customerInactiveJsonConverter(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/inactiveJsonConverter.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 1000);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String restockJsonConverter(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/stockAlertJsonConverter.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 1000);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String sellingTrendJsonConverter(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/sellingTrendJsonConverter.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 1000);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String mydioCall(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/mydio/find.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 1000);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String mydioExec(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/mydio/execute.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 1000);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String mydioAnalysis(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/mydio/analysis.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 1000);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String mydioStart(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/mydio/start.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 1000);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }

    @Override
    public String ambiguousDetection(String message) {
        String role = "";
        try {
            String filePath = "src/main/resources/static/ambiguousDetection.txt"; // Update with the correct path to your file
            role = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); requestBody.put("temperature", 0.1);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", role),
                Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 1000);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
                String fullResponse = (String) messageMap.get("content");

                return fullResponse;
            }
        } else {
            throw new RuntimeException("Failed to get response from OpenAI");
        }
        return null;
    }


    private String extractSQLQuery(String fullResponse) {
        String sqlQuery = "";
        int startIndex = fullResponse.indexOf("```sql") + 6;
        int endIndex = fullResponse.indexOf("```", startIndex);

        if (startIndex != -1 && endIndex != -1) {
            sqlQuery = fullResponse.substring(startIndex, endIndex).trim();
        }
        return sqlQuery;
    }

    private String extractJSONQuery(String fullResponse) {
        String sqlQuery = "";
        int startIndex = fullResponse.indexOf("```json") + 6;
        int endIndex = fullResponse.indexOf("```", startIndex);

        if (startIndex != -1 && endIndex != -1) {
            sqlQuery = fullResponse.substring(startIndex, endIndex).trim();
        }
        return sqlQuery;
    }
}