package Viettel.backend.service.textextractor;

import Viettel.backend.model.ChatDocument;
import Viettel.backend.model.MetadataDocument;

import java.util.Map;

public class TextExtractorFactory {

    public static <T> TextExtractor<T> getExtractor(T document) {
        if (document instanceof ChatDocument) {
            return (TextExtractor<T>) new ChatTextExtractor();
        } else if (document instanceof MetadataDocument) {
            return (TextExtractor<T>) new MetadataTextExtractor();
        } else if (document instanceof Map) {
            return (TextExtractor<T>) new MapTextExtractor();
        }
        throw new IllegalArgumentException("No extractor found for document type: " + document.getClass());
    }
}
