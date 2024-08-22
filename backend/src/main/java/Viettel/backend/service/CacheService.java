package Viettel.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Service for managing semantic and in-memory caching using Redis.
 */
@Service
public class SemanticCacheService {

    private static final Logger logger = LoggerFactory.getLogger(SemanticCacheService.class);
    private static final String EMBEDDING_KEY_PREFIX = "semantic:embedding:";
    private static final String RESPONSE_KEY_PREFIX = "semantic:response:";

    private final JedisPooled jedisPooled;
    private final EmbeddingService embeddingService;

    @Autowired
    public SemanticCacheService(JedisPooled jedisPooled, EmbeddingService embeddingService) {
        this.jedisPooled = jedisPooled;
        this.embeddingService = embeddingService;
    }

    /**
     * Caches the response for a given query using its embedding.
     *
     * @param query    The query text.
     * @param response The response to cache.
     */
    public void cacheSemanticResponse(String query, String response) {
        float[] embedding = embeddingService.generateEmbedding(query);
        String hashOfEmbedding = generateEmbeddingId(embedding);

        // Store the embedding and response in a Redis hash
        String responseKey = RESPONSE_KEY_PREFIX + hashOfEmbedding;

        jedisPooled.hset(responseKey, "embedding", serializeEmbedding(embedding));
        jedisPooled.hset(responseKey, "response", response);
        jedisPooled.expire(responseKey, determineTTL(query)); // Set a dynamic TTL based on query characteristics

        logger.info("Cached response for query with hash: {}", hashOfEmbedding);
    }

    /**
     * Retrieves a cached response based on the query's embedding.
     *
     * @param query The query text.
     * @return The cached response if found, or null.
     */
    public String getSemanticCachedResponse(String query) {
        float[] queryEmbedding = embeddingService.generateEmbedding(query);
        String hashOfEmbedding = generateEmbeddingId(queryEmbedding);

        String responseKey = RESPONSE_KEY_PREFIX + hashOfEmbedding;
        String cachedResponse = jedisPooled.hget(responseKey, "response");

        if (cachedResponse != null) {
            logger.info("Cache hit for query with hash: {}", hashOfEmbedding);
        } else {
            logger.info("Cache miss for query with hash: {}", hashOfEmbedding);
        }

        return cachedResponse;
    }

    /**
     * Generates a unique ID for an embedding using hashing.
     *
     * @param embedding The embedding array.
     * @return A unique hash string representing the embedding.
     */
    private String generateEmbeddingId(float[] embedding) {
        return Hashing.sha256()
                .hashString(Arrays.toString(embedding), StandardCharsets.UTF_8)
                .toString();
    }

    /**
     * Serializes an embedding array to a string.
     *
     * @param embedding The embedding array.
     * @return The serialized embedding as a string.
     */
    private String serializeEmbedding(float[] embedding) {
        return Arrays.stream(embedding)
                .mapToObj(Float::toString)
                .collect(Collectors.joining(","));
    }

    /**
     * Determines an appropriate TTL for a cached response.
     *
     * @param query The query text.
     * @return The TTL in seconds.
     */
    private int determineTTL(String query) {
        // Example logic for dynamic TTL: shorter TTL for shorter queries
        if (query.length() < 50) {
            return 1800; // 30 minutes for short queries
        } else {
            return 3600; // 1 hour for longer, more complex queries
        }
    }
}
