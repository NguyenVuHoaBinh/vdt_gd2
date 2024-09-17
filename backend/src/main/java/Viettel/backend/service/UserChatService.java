package Viettel.backend.service;

import Viettel.backend.model.PostgreSQLUserChat;
import Viettel.backend.postgredb.UserChatMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserChatService {

    @Autowired
    private UserChatMetadata userChatMetadata;

    public void saveUserChat(String Session_id, String message, String query_result) {
        PostgreSQLUserChat userchat = new PostgreSQLUserChat();
        userchat.setSession_id(Session_id);
        userchat.setUser_message(message); // JSON in String format
        userchat.setQuery_result(query_result);
        userchat.setCreatedAt(LocalDateTime.now());

        userChatMetadata.saveUserChat(userchat);
    }
}
