package Viettel.backend.service.cacheservice;

import Viettel.backend.service.llmservice.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SemanticCacheService {

    private static final Logger logger = LoggerFactory.getLogger(SemanticCacheService.class);
    private static final String INDEX_NAME = "semantic_cache_index";
    private static final String VECTOR_FIELD_NAME = "embedding";
    private static final int EMBEDDING_DIMENSION = 1536;

    private final JedisPooled jedisPooled;
    private final EmbeddingService embeddingService;

    @Autowired
    public SemanticCacheService(JedisPooled jedisPooled, EmbeddingService embeddingService) {
        this.jedisPooled = jedisPooled;
        this.embeddingService = embeddingService;

        // Ensure the index is created
        createIndexIfNotExists();
    }

    private void createIndexIfNotExists() {
        try {
            // Attempt to describe the index to check if it already exists
            jedisPooled.ftInfo(INDEX_NAME);
            logger.info("RediSearch index '{}' already exists.", INDEX_NAME);
        } catch (Exception e) {
            // If the index does not exist, an exception will be thrown
            logger.info("Index '{}' does not exist. Creating a new one.", INDEX_NAME);

            try {
                // Define the schema
                Schema schema = new Schema()
                        .addTextField("message", 1.0)  // Field to store user messages for searching
                        .addTextField("response", 1.0) // Ensure response is also a searchable text field
                        .addVectorField(
                                VECTOR_FIELD_NAME,
                                Schema.VectorField.VectorAlgo.HNSW,
                                Map.of(
                                        "TYPE", "FLOAT32",
                                        "DIM", EMBEDDING_DIMENSION,
                                        "DISTANCE_METRIC", "COSINE"
                                )
                        );

                // Define index options with prefixes
                IndexDefinition indexDefinition = new IndexDefinition().setPrefixes("semantic:doc:");

                // Create the index
                jedisPooled.ftCreate(INDEX_NAME, IndexOptions.defaultOptions().setDefinition(indexDefinition), schema);

                logger.info("Created RediSearch index for semantic cache.");
            } catch (Exception ex) {
                logger.error("Failed to create RediSearch index '{}'. Error: {}", INDEX_NAME, ex.getMessage(), ex);
            }
        }
    }

    public void cacheResponse(String query, String response) {
        try {
            // Generate the embedding vector for the query
            float[] embeddingVector = embeddingService.generateEmbedding(query);

            // Create a unique ID for the document using SHA-256
            String documentId = generateDocumentId(query);

            // Convert the float array to a byte array for storage
            byte[] embeddingBytes = floatToByte(embeddingVector);

            // Prepare the fields as byte arrays
            Map<byte[], byte[]> binaryFields = new HashMap<>();
            binaryFields.put("message".getBytes(StandardCharsets.UTF_8), query.getBytes(StandardCharsets.UTF_8));
            binaryFields.put("response".getBytes(StandardCharsets.UTF_8), response.getBytes(StandardCharsets.UTF_8));
            binaryFields.put(VECTOR_FIELD_NAME.getBytes(StandardCharsets.UTF_8), embeddingBytes);

            // Store all fields in the same hash using byte arrays
            jedisPooled.hset(documentId.getBytes(StandardCharsets.UTF_8), binaryFields);

            logger.info("Cached query '{}' with response and embedding.", query);
        } catch (Exception e) {
            logger.error("Failed to cache query '{}'. Error: {}", query, e.getMessage(), e);
        }
    }

    private String generateDocumentId(String query) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(query.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return "semantic:doc:" + hexString.toString();
    }

    // Method to convert a float array to a byte array using ByteBuffer
    private byte[] floatToByte(float[] input) {
        ByteBuffer buffer = ByteBuffer.allocate(input.length * 4);
        for (float f : input) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    // Method to perform hybrid semantic search using vector and non-vector criteria
    public String performHybridSearch(String query) {
        try {
            // Generate the embedding vector for the input query
            float[] queryEmbedding = embeddingService.generateEmbedding(query);
            byte[] embeddingBytes = floatToByte(queryEmbedding);

            int K = 1;
            String hybridQuery = "*" + "=>[KNN " + K + " @embedding $BLOB]";

            // Prepare the FT.SEARCH command with the hybrid query
            Query searchQuery = new Query(hybridQuery)
                    .addParam("BLOB", embeddingBytes)
                    .returnFields("response", "message")
                    .limit(0, K)
                    .dialect(2); // Use dialect 2 for the KNN functionality

            // Execute the search
            SearchResult searchResult = jedisPooled.ftSearch(INDEX_NAME, searchQuery);

            if (searchResult.getTotalResults() > 0) {
                // Return the response from the top result
                Document topResult = searchResult.getDocuments().get(0);
                return topResult.getString("response");
            }
        } catch (Exception e) {
            logger.error("Failed to perform hybrid search for query '{}'. Error: {}", query, e.getMessage(), e);
        }

        return null; // No similar query found
    }

    // Method to convert byte array back to float array
    private float[] byteArrayToFloatArray(byte[] byteArray) {
        float[] floatArray = new float[byteArray.length / 4];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        for (int i = 0; i < floatArray.length; i++) {
            floatArray[i] = buffer.getFloat();
        }
        return floatArray;
    }
}
