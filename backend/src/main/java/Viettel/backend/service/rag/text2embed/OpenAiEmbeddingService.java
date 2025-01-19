package Viettel.backend.service.rag.text2embed;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

// TODO: convert to https://docs.spring.io/spring-ai/reference/getting-started.html
// TODO embed batch
@Service
public class OpenAiEmbeddingService implements EmbeddingService {
    @Value("${openai.api.key}")
    private String OPENAI_KEY;

    @Value("${openai.embedding.url}")
    private String OPENAI_EMBEDDING_URL;

    @Setter
    private String model;

//    @Setter
//    private Encoding encoding;

//    @Autowired
//    private EncodingRegistry registry;
//
//    public void setEncoding() {
//        encoding = registry.getEncodingForModel(model).get();
//    }

    public List<float[]> embed(List<String> texts) {
        // TODO resttemplate initialization
        RestTemplate restTemplate = new RestTemplate();

        // Prepare the request payload
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", texts);
        requestBody.put("model", model);

        // Set up the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(OPENAI_KEY);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Make the POST request
        ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_EMBEDDING_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        // TODO: response parser
        // Handle the response
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");

            List<float[]> embeddings = new ArrayList<>();
            for (Map<String, Object> item : data) {
                List<Double> embeddingList = (List<Double>) item.get("embedding");

                float[] embeddingArray = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    embeddingArray[i] = embeddingList.get(i).floatValue();
                }

                embeddings.add(embeddingArray);
            }
            return embeddings;
        } else {
            // Handle errors appropriately
            throw new RuntimeException("Failed to get embeddings from OpenAI API. Status code: " + response.getStatusCode());
        }
    }

//    public String ensureTextLimit(String text) {
//        int maxContextLength = ModelType.fromName(model)
//                                 .get()
//                                 .getMaxContextLength();
//
//        int nTokens = encoding.countTokens(text);
//        if (nTokens <= maxContextLength) return text;
//
//        // TODO: to log
//        System.err.printf("The prompt has been truncated from %s tokens to %s tokens\n" +
//                "to git within the max token limit. REduce the length of the prompt to prevent it from being cut off.\n"
//        , nTokens, maxContextLength);
//
//        IntArrayList encoded = encoding.encode(text);
//        encoding.




//    }
}


