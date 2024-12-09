package Viettel.backend.service.textextractor;

import Viettel.backend.model.ChatDocument;

import java.util.Map;

public class ChatTextExtractor implements TextExtractor<ChatDocument> {
    @Override
    public String extractText(Map<String, Object> document) {
        return "";
    }

    @Override
    public String extract(ChatDocument document) {
        return document.getMessage(); // Extract the message from ChatDocument
    }
}

