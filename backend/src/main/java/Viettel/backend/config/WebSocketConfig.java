package Viettel.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import Viettel.backend.logs_tracing.WebSocketLogHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketLogHandler webSocketLogHandler;

    public WebSocketConfig(WebSocketLogHandler webSocketLogHandler) {
        this.webSocketLogHandler = webSocketLogHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketLogHandler, "/logs").setAllowedOrigins("*");
    }
}
