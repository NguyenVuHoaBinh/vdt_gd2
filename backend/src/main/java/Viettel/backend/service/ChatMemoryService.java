package Viettel.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class ChatMemoryService {

    private static final String CHAT_SESSION_KEY_PREFIX = "chat:session:";
    private static final String METADATA_KEY_SUFFIX = ":metadata";

    @Autowired
    private JedisCluster jedisCluster;

    public void storeSessionMetadata(String sessionId, Map<String, String> metadata) {
        String key = CHAT_SESSION_KEY_PREFIX + sessionId + METADATA_KEY_SUFFIX;
        jedisCluster.hmset(key, metadata);
    }

    public Map<String, String> getSessionMetadata(String sessionId) {
        String key = CHAT_SESSION_KEY_PREFIX + sessionId + METADATA_KEY_SUFFIX;
        return jedisCluster.hgetAll(key);
    }

    public void storeUserChat(String sessionId, String user, String message) {
        String key = CHAT_SESSION_KEY_PREFIX + sessionId;
        jedisCluster.rpush(key, user + ": " + message);
    }

    public List<String> getUserChatHistory(String sessionId) {
        String key = CHAT_SESSION_KEY_PREFIX + sessionId;
        return jedisCluster.lrange(key, 0, -1);
    }

    public void clearChatSession(String sessionId) {
        String key = CHAT_SESSION_KEY_PREFIX + sessionId;
        jedisCluster.del(key);
        jedisCluster.del(key + METADATA_KEY_SUFFIX);
    }
}
