package Viettel.backend.logs_tracing;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class SQLLogWebAppender extends AppenderBase<ILoggingEvent> {

    private static WebSocketLogHandler webSocketLogHandler;

    public static void setWebSocketLogHandler(WebSocketLogHandler handler) {
        webSocketLogHandler = handler;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if ("Viettel.backend.controller.SQLChatController".equals(eventObject.getLoggerName()) && webSocketLogHandler != null) {
            String logMessage = eventObject.getFormattedMessage();
            webSocketLogHandler.broadcastLog(logMessage);
        }
    }
}
