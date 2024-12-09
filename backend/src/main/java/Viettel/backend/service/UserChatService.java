package Viettel.backend.service;

import Viettel.backend.model.PostgreSQLUserChat;
import Viettel.backend.postgredb.UserChatMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Service
public class UserChatService {

    private static final Logger logger = LoggerFactory.getLogger(UserChatService.class);

    @Autowired
    private UserChatMetadata userChatMetadata;

    public void saveUserChat(String sessionId, String message, String queryResult) {
        try {
            logger.info("Saving user chat for sessionId: {}", sessionId);
            logger.debug("User message: {}", message);
            logger.debug("Query result: {}", queryResult);

            PostgreSQLUserChat userchat = new PostgreSQLUserChat();
            userchat.setSession_id(sessionId);
            userchat.setUser_message(message); // JSON in String format
            userchat.setQuery_result(queryResult);
            userchat.setCreatedAt(LocalDateTime.now());

            userChatMetadata.saveUserChat(userchat);
            logger.info("User chat saved successfully for sessionId: {}", sessionId);
        } catch (Exception e) {
            logger.error("Failed to save user chat for sessionId: {}", sessionId, e);
        }
    }
}
