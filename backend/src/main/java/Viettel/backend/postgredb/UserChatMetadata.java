package Viettel.backend.postgredb;

import Viettel.backend.model.PostgreSQLUserChat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class UserChatMetadata {
    private final JdbcTemplate jdbcTemplate;

    public UserChatMetadata(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveUserChat(PostgreSQLUserChat metadata) {
        String sql = "INSERT INTO chat (session_id, user_message, query_result, created_at) VALUES (?,?,?::jsonb,?)";
        jdbcTemplate.update(
                sql,
                metadata.getSession_id(),
                metadata.getUser_message(),
                metadata.getQuery_result(),
                metadata.getCreatedAt()
        );
    }
}
