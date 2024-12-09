package Viettel.backend.model;

import java.util.Date;

public class ChatDocument {

    private String userId;
    private Date timestamp;
    private String message;

    // Default constructor (required for some frameworks/libraries)
    public ChatDocument() {}

    public ChatDocument(String userId, Date timestamp, String message) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.message = message;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
