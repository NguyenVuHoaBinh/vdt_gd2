package Viettel.backend.service.llmservice;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component("LLMServiceFactory")
public class LLMServiceFactory {
    private final RestTemplate restTemplate;
    private final Map<String, Function<String, LLMService>> llms = new HashMap<>();

    public LLMServiceFactory(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        // TODO full lists of models here
        // open ai
        llms.put("gpt-3.5-turbo", model -> new OpenAiService(restTemplate, model));
        llms.put("gpt-4o-mini", model -> new OpenAiService(restTemplate, model));
        llms.put("gpt-4o", model -> new OpenAiService(restTemplate, model)); // Uses the same pattern for consistency

        // gemini
        llms.put("gemini", model -> new GeminiService(restTemplate));
    }

    public LLMService createLLMService(String model) {
        String MODEL = model.toLowerCase();
        Function<String, LLMService> llmService = llms.get(MODEL);

        // Default model is defined in OpenAiService
        if (llmService == null)
            llmService = defaultModel -> new OpenAiService(restTemplate);

        return llmService.apply(MODEL);

    }
}
