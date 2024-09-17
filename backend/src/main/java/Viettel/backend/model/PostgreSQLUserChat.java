package Viettel.backend.model;

import java.time.LocalDateTime;

public class PostgreSQLUserChat {
    private String id;
    private String session_id;
    private String user_message;
    private String query_result;
    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getUser_message() {
        return user_message;
    }

    public void setUser_message(String user_message) {
        this.user_message = user_message;
    }

    public String getQuery_result() {
        return query_result;
    }

    public void setQuery_result(String query_result) {
        this.query_result = query_result;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
