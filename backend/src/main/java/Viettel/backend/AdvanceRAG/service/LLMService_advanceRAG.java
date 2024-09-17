package Viettel.backend.AdvanceRAG.service;

import Viettel.backend.controller.SQLChatController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class LLMService_advanceRAG {
    private static final Logger logger = LoggerFactory.getLogger(SQLChatController.class);

    @Value("${openai.api.key}")
    private final String openaiApiKey;


    private final RestTemplate restTemplate;
    private final String openaiApiUrl = "https://api.openai.com/v1/chat/completions";

    public LLMService_advanceRAG(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.openaiApiKey = System.getenv("OPENAI_API_KEY"); // Ensure this is securely stored
    }

    // Generate refined query
    public String generateRefinedQuery(String userQuery) {
        String prompt = """
                You are an assistant designed to refine natural language queries into key elements for database searches. The goal is to identify important keywords, entities, and concepts in the user's query that will be useful for finding relevant database schemas later. Ignore common words that do not contribute to the search (e.g., "the," "a," "is," "show," "list"). Focus on identifying names, attributes, actions, and timeframes.
                
                ### Examples of User Queries and Expected Output:
                1. User Query: "List all customers who joined this year"
                   Output: "customers, joined, this year"
                
                2. User Query: "Show me the names and emails of active employees"
                   Output: "names, emails, active employees"
                
                3. User Query: "How many orders were placed in the last month?"
                   Output: "orders, placed, last month"
                
                4. User Query: "Retrieve details of products with low stock"
                   Output: "details, products, low stock"
                
                5. User Query: "Find total sales revenue for the current quarter"
                   Output: "total sales revenue, current quarter"
                
                ### Task:
                Given the user's query, identify the most important keywords, entities, and concepts. Ignore common words and focus on names, attributes, actions, and timeframes. Provide the result as a comma-separated list of keywords.
                THIS IS THE BEGIN OF USER QUERY:
                
                """
                + userQuery;
        return callOpenAIChatCompletion(prompt);
    }

    // Generate hypothetical document (HyDE)
    public String generateHyDE(String query) {
        String prompt = """
                Generate a comprehensive hypothetical document that explains how to identify all customers who have placed more than five orders in the last month and calculate their total order amount. The document should include:
                
                1. **Database Schema Overview:**
                   - Describe the relevant tables (e.g., Customers, Orders) and their columns.
                   - Explain the relationships between these tables.
                
                2. **Step-by-Step SQL Query Construction:**
                   - Filtering orders within the last month.
                   - Grouping orders by customer.
                   - Counting the number of orders per customer.
                   - Calculating the total order amount per customer.
                   - Applying the condition to select customers with more than five orders.
                   - Joining tables to retrieve customer details.
                
                3. **Sample SQL Query:**
                   - Provide an example SQL query that accomplishes the task.
                
                4. **Explanation of Each Clause:**
                   - Break down the SQL query, explaining the purpose of each part.
                
                Ensure the document is detailed enough to guide someone in constructing the SQL query accurately.
                
                Below is the original query
                
                """ + query;
        return callOpenAIChatCompletion(prompt);
    }

    // Generate answer based on context
    public String generateAnswer(String query, String context, String role) {
        String prompt = role
                +"\nUse the following context to answer the question:\n\nContext:\n" + context + "\n\nQuestion:\n" + query;
        return callOpenAIChatCompletion(prompt);
    }

    // Call OpenAI Chat Completion API
    private String callOpenAIChatCompletion(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        // Create the request body with the 'messages' parameter
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 200);
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(openaiApiUrl, HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (message != null) ? (String) message.get("content") : null;
                }
            }
            logger.error("Error: Response did not contain expected data.");
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Error occurred while calling OpenAI API", e);
            return null;
        }
    }

}

