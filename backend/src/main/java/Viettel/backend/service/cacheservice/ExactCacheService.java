package Viettel.backend.service.cacheservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ExactCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ExactCacheService.class);
    private static final String GENERAL_CACHE_PREFIX = "cache:response:";
    private static final int DEFAULT_TTL_SHORT = 1800; // 30 minutes
    private static final int DEFAULT_TTL_MEDIUM = 3600; // 1 hour
    private static final int DEFAULT_TTL_LONG = 7200; // 2 hours

    private final JedisPooled jedisPooled;
    private final ConcurrentMap<String, Integer> queryHitCounts = new ConcurrentHashMap<>();

    @Autowired
    public ExactCacheService(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
    }

    /**
     * Initializes scheduled tasks for cache hit count cleanup.
     */
    @PostConstruct
    public void init() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cleanUpHitCounts, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Cleans up hit counts for queries with low access frequency.
     */
    private void cleanUpHitCounts() {
        queryHitCounts.entrySet().removeIf(entry -> entry.getValue() < 5);
        logger.debug("Cleaned up hit counts for low-frequency queries.");
    }

    /**
     * Caches the exact response for a given query with a dynamically determined TTL.
     *
     * @param query    the user query
     * @param response the response to cache
     */
    public void cacheExactResponse(String query, String response) {
        if (!isValidQuery(query) || !isValidResponse(response)) {
            logger.warn("Invalid query or response provided for caching.");
            return;
        }

        String cacheKey = GENERAL_CACHE_PREFIX + hashQuery(query);
        if (!isValidCacheKey(cacheKey)) {
            logger.warn("Generated cache key is invalid or too long for query: {}", maskQuery(query));
            return;
        }

        int ttl = determineTTL(query);
        try {
            jedisPooled.setex(cacheKey, ttl, response);
            logger.info("Cached exact response for query: {}", maskQuery(query));
        } catch (Exception e) {
            logger.error("Failed to cache response for query: {}", maskQuery(query), e);
        }
    }

    /**
     * Retrieves the cached response for a given query.
     *
     * @param query the user query
     * @return the cached response if present; otherwise, null
     */
    public String getExactCachedResponse(String query) {
        if (!isValidQuery(query)) {
            logger.warn("Invalid query provided for cache retrieval.");
            return null;
        }

        String cacheKey = GENERAL_CACHE_PREFIX + hashQuery(query);
        if (!isValidCacheKey(cacheKey)) {
            logger.warn("Generated cache key is invalid or too long for query: {}", maskQuery(query));
            return null;
        }

        try {
            String cachedResponse = jedisPooled.get(cacheKey);
            if (cachedResponse != null) {
                incrementHitCount(query);
                logger.info("Exact cache hit for query: {}", maskQuery(query));
            } else {
                logger.info("Exact cache miss for query: {}", maskQuery(query));
            }
            return cachedResponse;
        } catch (Exception e) {
            logger.error("Failed to retrieve cached response for query: {}", maskQuery(query), e);
            return null;
        }
    }

    /**
     * Invalidates the cached response for a specific query.
     *
     * @param query the user query
     */
    public void invalidateCacheForQuery(String query) {
        if (!isValidQuery(query)) {
            logger.warn("Invalid query provided for cache invalidation.");
            return;
        }

        String cacheKey = GENERAL_CACHE_PREFIX + hashQuery(query);
        if (!isValidCacheKey(cacheKey)) {
            logger.warn("Generated cache key is invalid or too long for query: {}", maskQuery(query));
            return;
        }

        try {
            jedisPooled.del(cacheKey);
            queryHitCounts.remove(query); // Reset hit count upon invalidation
            logger.info("Invalidated cache for query: {}", maskQuery(query));
        } catch (Exception e) {
            logger.error("Failed to invalidate cache for query: {}", maskQuery(query), e);
        }
    }

    /**
     * Determines the TTL based on query characteristics.
     *
     * @param query the user query
     * @return the TTL in seconds
     */
    private int determineTTL(String query) {
        int hits = queryHitCounts.getOrDefault(query, 0);
        if (hits > 10) {
            return DEFAULT_TTL_LONG; // 2 hours for frequently accessed queries
        } else if (query.length() < 50) {
            return DEFAULT_TTL_SHORT; // 30 minutes for short queries
        } else {
            return DEFAULT_TTL_MEDIUM; // 1 hour for other queries
        }
    }

    /**
     * Increments the hit count for a query.
     *
     * @param query the user query
     */
    private void incrementHitCount(String query) {
        queryHitCounts.merge(query, 1, Integer::sum);
    }

    /**
     * Generates a SHA-256 hash for the given query.
     *
     * @param query the user query
     * @return the hexadecimal representation of the hash
     */
    public String hashQuery(String query) {
        return Hashing.sha256()
                .hashString(query, StandardCharsets.UTF_8)
                .toString();
    }

    /**
     * Validates the cache key.
     *
     * @param cacheKey the cache key to validate
     * @return true if valid; otherwise, false
     */
    private boolean isValidCacheKey(String cacheKey) {
        return cacheKey != null && cacheKey.length() <= 256; // Example limit
    }

    /**
     * Validates the query.
     *
     * @param query the query to validate
     * @return true if valid; otherwise, false
     */
    private boolean isValidQuery(String query) {
        return query != null && !query.trim().isEmpty();
    }

    /**
     * Validates the response.
     *
     * @param response the response to validate
     * @return true if valid; otherwise, false
     */
    private boolean isValidResponse(String response) {
        return response != null && !response.trim().isEmpty();
    }

    /**
     * Masks the query for logging to prevent exposing sensitive information.
     *
     * @param query the original query
     * @return a masked version of the query
     */
    private String maskQuery(String query) {
        if (query.length() <= 10) {
            return "****" + query;
        }
        return "****" + query.substring(query.length() - 10);
    }
}
