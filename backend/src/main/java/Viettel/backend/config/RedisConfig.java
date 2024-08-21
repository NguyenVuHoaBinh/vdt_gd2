package Viettel.backend.config;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.RedisVectorStore.RedisVectorStoreConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPooled;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class RedisConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;


    @Bean
    public OpenAiApi openAiApi() {
        // Replace "YOUR_API_KEY" with your actual OpenAI API key
        return new OpenAiApi(openAiApiKey);
    }

    @Bean
    public JedisPooled jedisPooled() {
        // Connect to Redis node
        return new JedisPooled("localhost", 6377);
    }




    @Bean
    public OpenAiEmbeddingModel embeddingModel(OpenAiApi openAiApi) {
        // Configure OpenAiEmbeddingModel with your OpenAiApi
        return new OpenAiEmbeddingModel(openAiApi);
    }

    @Bean
    public RedisVectorStore redisVectorStore(JedisPooled jedisPooled, OpenAiEmbeddingModel embeddingModel) {
        // Configure RedisVectorStore
        RedisVectorStoreConfig config = RedisVectorStoreConfig.builder()
                .withIndexName("my-vector-index")
                .withPrefix("embedding:")
                .withContentFieldName("content")
                .withEmbeddingFieldName("embedding")
                .withVectorAlgorithm(RedisVectorStore.Algorithm.HSNW) // or FLAT
                .build();

        return new RedisVectorStore(config, embeddingModel, jedisPooled, true);
    }
}