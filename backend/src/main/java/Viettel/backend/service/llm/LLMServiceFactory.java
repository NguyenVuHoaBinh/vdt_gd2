package Viettel.backend.service.llm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class LLMServiceFactory {

    @Autowired
    private ApplicationContext context;

    private final Map<String, Function<String, LLMService>> llms = new HashMap<>();

    public LLMServiceFactory() {
        // TODO full list of models here
        // open ai
        llms.put("gpt-3.5-turbo", this::createOpenAiService);
        llms.put("gpt-4o-mini", this::createOpenAiService);
        llms.put("gpt-4o", this::createOpenAiService);

        // gemini
        llms.put("gemini", this::createGeminiService);
    }

    public LLMService createLLMService(String model) {
        String MODEL = model.toLowerCase();
        Function<String, LLMService> llmService = llms.get(MODEL);

        if (llmService == null) {
            MODEL = "gpt-4o-mini";
            llmService = this::createOpenAiService;
        }
        return llmService.apply(MODEL);
    }

    // factories~
    private LLMService createOpenAiService(String model) {
        OpenAiService service = context.getBean(OpenAiService.class);
        service.setModel(model);
        return service;
    }

    private LLMService createGeminiService(String model) {
        GeminiService service = context.getBean(GeminiService.class);
        service.setModel(model);
        return service;
    }
}
