package Viettel.backend.config;

import Viettel.backend.logs_tracing.SQLLogWebAppender;
import Viettel.backend.logs_tracing.WebSocketLogHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public WebSocketLogHandler webSocketLogHandler() {
        WebSocketLogHandler handler = new WebSocketLogHandler();
        SQLLogWebAppender.setWebSocketLogHandler(handler);
        return handler;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
