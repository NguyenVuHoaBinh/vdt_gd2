package Viettel.backend.service.textextractor;

import java.util.Map;
import java.util.stream.Collectors;

public class MapTextExtractor implements TextExtractor<Map<String, Object>> {

    @Override
    public String extractText(Map<String, Object> document) {
        // Extract text from the map as needed. Here, we simply concatenate key-value pairs.
        return document.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    @Override
    public String extract(Map<String, Object> document) {
        return "";
    }
}
