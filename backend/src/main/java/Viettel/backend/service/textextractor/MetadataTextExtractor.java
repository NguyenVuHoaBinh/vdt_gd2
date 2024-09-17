package Viettel.backend.service.textextractor;


import Viettel.backend.model.MetadataDocument;

import java.util.Map;

public class MetadataTextExtractor implements TextExtractor<MetadataDocument> {
    @Override
    public String extractText(Map<String, Object> document) {
        return "";
    }

    @Override
    public String extract(MetadataDocument document) {
        return document.getOriginalText(); // Extract schema details from MetadataDocument
    }
}
