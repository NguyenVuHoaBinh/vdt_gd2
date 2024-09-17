package Viettel.backend.service.metadataservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

import java.util.Map;

@Service
public class MetadataStorageService {

    private final JedisPooled jedisPooled;

    @Autowired
    public MetadataStorageService(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
    }

    /**
     * Stores metadata in Redis.
     *
     * @param sessionId The session ID.
     * @param metadataType The type of metadata (e.g., "schema", "session", "chat").
     * @param metadata The metadata to store.
     */
    public void storeMetadata(String sessionId, String metadataType, Map<String, String> metadata) {
        String redisKey = "metadata:" + sessionId + ":" + metadataType;
        jedisPooled.hset(redisKey, metadata);
    }

    /**
     * Retrieves metadata from Redis.
     *
     * @param sessionId The session ID.
     * @param metadataType The type of metadata (e.g., "schema", "session", "chat").
     * @return The retrieved metadata as a Map.
     */
    public Map<String, String> getMetadata(String sessionId, String metadataType) {
        String redisKey = "metadata:" + sessionId + ":" + metadataType;
        return jedisPooled.hgetAll(redisKey);
    }

    /**
     * Deletes metadata from Redis.
     *
     * @param sessionId The session ID.
     * @param metadataType The type of metadata (e.g., "schema", "session", "chat").
     */
    public void deleteMetadata(String sessionId, String metadataType) {
        String redisKey = "metadata:" + sessionId + ":" + metadataType;
        jedisPooled.del(redisKey);
    }
}
