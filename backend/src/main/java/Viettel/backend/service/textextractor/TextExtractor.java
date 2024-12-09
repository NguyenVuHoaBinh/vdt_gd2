package Viettel.backend.service.textextractor;

import java.util.Map;

public interface TextExtractor<T> {
    String extractText(Map<String, Object> document);

    String extract(T document);
}

