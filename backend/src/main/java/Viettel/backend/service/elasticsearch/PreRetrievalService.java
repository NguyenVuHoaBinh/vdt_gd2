package Viettel.backend.service.elasticsearch;

import org.springframework.stereotype.Service;

@Service
public class PreRetrievalService {

    public String expandQuery(String query) {
        // Example of expanding a query with related terms
        if (query.equalsIgnoreCase("AI")) {
            return "AI artificial intelligence machine learning";
        }
        return query;
    }
}
