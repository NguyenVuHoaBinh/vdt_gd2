package Viettel.backend.config;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration class for managing OpenAI API dependencies.
 */
@Configuration
public class OpenAiApiConfiguration {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(apiKey);
    }
}
