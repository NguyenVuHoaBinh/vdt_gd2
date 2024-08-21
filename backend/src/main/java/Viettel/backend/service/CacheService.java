package Viettel.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

@Service
public class CacheService {

    private final JedisPooled jedisPooled;

    @Autowired
    public CacheService(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
    }

    public void cacheResponse(String prompt, String response) {
        jedisPooled.setex("cache:response:" + prompt, 3600, response); // Cache for 1 hour
    }

    public String getCachedResponse(String prompt) {
        return jedisPooled.get("cache:response:" + prompt);
    }
}
