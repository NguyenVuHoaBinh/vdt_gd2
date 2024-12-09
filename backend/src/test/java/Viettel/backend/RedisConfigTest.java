package Viettel.backend;

import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.JedisPooled;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RedisConfig to ensure correct configuration of beans.
 */
@SpringBootTest
class RedisConfigTest {

    @Autowired
    private OpenAiApi openAiApi;

    @Autowired
    private JedisPooled jedisPooled;

    @Autowired
    private OpenAiEmbeddingModel embeddingModel;

    @Autowired
    private RedisVectorStore redisVectorStore;

    @Test
    void testOpenAiApiBean() {
        assertNotNull(openAiApi, "OpenAiApi bean should be configured and not null");
    }

    @Test
    void testJedisPooledBean() {
        assertNotNull(jedisPooled, "JedisPooled bean should be configured and not null");
        assertEquals("PONG", jedisPooled.ping(), "JedisPooled should be able to ping the Redis server");
    }

    @Test
    void testEmbeddingModelBean() {
        assertNotNull(embeddingModel, "OpenAiEmbeddingModel bean should be configured and not null");
    }

    @Test
    void testRedisVectorStoreBean() {
        assertNotNull(redisVectorStore, "RedisVectorStore bean should be configured and not null");
    }
}
