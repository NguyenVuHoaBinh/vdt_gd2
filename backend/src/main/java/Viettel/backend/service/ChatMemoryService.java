package Viettel.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

import java.util.Map;

@Service
public class ChatMemoryService {

    private final JedisPooled jedisPooled;

    @Autowired
    public ChatMemoryService(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
    }

    // Store metadata in Redis with the session ID as the key
    public void storeSessionMetadata(String sessionId, Map<String, String> metadata) {
        String redisKey = "session:" + sessionId + ":metadata";
        jedisPooled.hset(redisKey, metadata);
    }

    // Store user chat in Redis
    public void storeUserChat(String sessionId, String userType, String message) {
        String redisKey = "session:" + sessionId + ":chat";
        String messageKey = userType + ":" + System.currentTimeMillis();
        jedisPooled.hset(redisKey, messageKey, message);
    }

    // Retrieve metadata from Redis
    public Map<String, String> getSessionMetadata(String sessionId) {
        String redisKey = "session:" + sessionId + ":metadata";
        return jedisPooled.hgetAll(redisKey);
    }

    // Retrieve chat history from Redis
    public Map<String, String> getUserChat(String sessionId) {
        String redisKey = "session:" + sessionId + ":chat";
        return jedisPooled.hgetAll(redisKey);
    }
}
