package Viettel.backend.config;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.EncodingRegistry;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration class for managing OpenAI API dependencies.
 */
@Configuration
public class OpenAiConfig {

    @Value("${openai.api.key}")
    private String OPENAI_KEY;

    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(OPENAI_KEY);
    }

//    @Bean
//    public EncodingRegistry encodingRegistry() {
//        return Encodings.newDefaultEncodingRegistry();
//    }
}
