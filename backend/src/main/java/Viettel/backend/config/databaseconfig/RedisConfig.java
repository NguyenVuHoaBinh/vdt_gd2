package Viettel.backend.config.databaseconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {

    @Bean
    public JedisPooled jedisPooled() {
        // Replace with your Redis host and port
        String redisHost = "localhost";
        int redisPort = 6377; // Replace with your Redis port
        return new JedisPooled(redisHost, redisPort);
    }
}
