package Viettel.backend.config.databaseconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {
    // Set up Redis host and port
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Bean
    public JedisPooled jedisPooled() {

        // Configure Jedis connection pool settings
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(100);        // Maximum number of connections
        poolConfig.setMaxIdle(30);         // Maximum idle connections in the pool
        poolConfig.setMinIdle(10);         // Minimum idle connections in the pool
        poolConfig.setMaxWaitMillis(3000); // Maximum wait time for a connection from the pool (in ms)
        poolConfig.setBlockWhenExhausted(true); // Block if pool is exhausted until a connection is available

        return new JedisPooled(poolConfig, redisHost, redisPort, 5000, redisPassword);
    }
}
