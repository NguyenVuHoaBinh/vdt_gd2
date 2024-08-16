package Viettel.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

@Service
public class CacheService {

    private static final String CACHE_KEY_PREFIX = "cache:llm:";

    @Autowired
    private JedisCluster jedisCluster;

    public void cacheResponse(String prompt, String response) {
        String key = CACHE_KEY_PREFIX + prompt.hashCode();
        jedisCluster.set(key, response);
    }

    public String getCachedResponse(String prompt) {
        String key = CACHE_KEY_PREFIX + prompt.hashCode();
        return jedisCluster.get(key);
    }

    public void clearCache(String prompt) {
        String key = CACHE_KEY_PREFIX + prompt.hashCode();
        jedisCluster.del(key);
    }
}
