package Viettel.backend.AdvanceRAG.service;

import Viettel.backend.model.MetadataDocument;
import Viettel.backend.service.elasticsearch.ElasticsearchService;
import Viettel.backend.service.llmservice.EmbeddingService;
import Viettel.backend.service.metadataservice.GraphQLService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocumentService {

    private final ElasticsearchService elasticsearchService;
    private final GraphQLService graphQLService;
    private final EmbeddingService embeddingService;
    private final Chunker chunker;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public DocumentService(Chunker chunker,
                           EmbeddingService embeddingService,
                           ElasticsearchService elasticsearchService,
                           GraphQLService graphQLService) {
        this.elasticsearchService = elasticsearchService;
        this.graphQLService = graphQLService;
        this.chunker = chunker;
        this.embeddingService = embeddingService;
    }

    public void processDocuments(String indexName, String databaseName) throws Exception {
        // Load documents from GraphQLService
        List<Map<String, Object>> documents = loadDocuments(databaseName);

        // Chunk documents
        List<Map<String, Object>> chunkedDocuments = chunker.sentenceWiseTokenizedChunkDocuments(
                documents, 512, 20, 50);

        // Embed and index chunked documents
        for (Map<String, Object> doc : chunkedDocuments) {
            String id = (String) doc.get("id");
            List<Integer> chunk = (List<Integer>) doc.get("chunk");

            String originalText = (String) doc.get("original_text");
            Integer chunkIndex = (Integer) doc.get("chunk_index");
            String parentId = (String) doc.get("parent_id");
            Integer chunkTokenCount = (Integer) doc.get("chunk_token_count");
            double[] embeddings = (double[]) doc.get("embedding"); // Directly using double[] for embedding

            // Create a MetadataDocument instance
            MetadataDocument metadataDocument = new MetadataDocument();
            metadataDocument.setId(id);
            metadataDocument.setChunk(chunk);
            metadataDocument.setOriginalText(originalText);
            metadataDocument.setChunkIndex(chunkIndex);
            metadataDocument.setParentId(parentId);
            metadataDocument.setChunkTokenCount(chunkTokenCount);
            metadataDocument.setEmbedding(embeddings); // Set double[] directly

            // Index the MetadataDocument into Elasticsearch
            elasticsearchService.indexMetadataDocument(indexName, id, metadataDocument);
        }
    }

    private List<Map<String, Object>> loadDocuments(String databaseName) throws Exception {
        List<Map<String, Object>> documents = new ArrayList<>();

        // Fetch the schema metadata as a JSON string
        String schemaJson = graphQLService.fetchDatabaseSchema(databaseName);

        // Parse the JSON string
        JsonNode rootNode = objectMapper.readTree(schemaJson);

        // Iterate over each table
        for (JsonNode tableNode : rootNode) {
            String tableName = tableNode.get("Table").asText();
            JsonNode fieldsNode = tableNode.get("Fields");

            // Build a text representation of the table schema
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("Table: ").append(tableName).append("\n");
            contentBuilder.append("Fields:\n");

            for (JsonNode fieldNode : fieldsNode) {
                String fieldName = fieldNode.get("Field").asText();
                String fieldType = fieldNode.get("Type").asText();
                contentBuilder.append("- ").append(fieldName).append(": ").append(fieldType).append("\n");
            }

            String content = contentBuilder.toString();

            // Create a document map
            Map<String, Object> doc = new HashMap<>();
            doc.put("id", UUID.randomUUID().toString());
            doc.put("table_name", tableName);
            doc.put("content", content);

            // Add the document to the list
            documents.add(doc);
        }

        return documents;
    }
}
