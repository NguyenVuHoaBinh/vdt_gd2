package Viettel.backend.test;

import redis.clients.jedis.JedisPooled;

public class RedisConnection {

    private JedisPooled jedisPooled;

    public RedisConnection() {
        // Initialize the connection to Redis
        String redisHost = "localhost";  // Replace with your Redis IP address
        int redisPort = 6377;
        String redisUser = "hoabinh12";      // Replace with your Redis username
        String redisPassword = "hoabinh12";  // Replace with your Redis password

        // Connect to Redis
        jedisPooled = new JedisPooled(redisHost, redisPort, redisUser, redisPassword);
    }

    public void testConnection() {
        try {
            // Simple PING to test the connection
            String response = jedisPooled.ping();
            System.out.println("Redis server response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedisPooled != null) {
                jedisPooled.close();
            }
        }
    }

    public static void main(String[] args) {
        RedisConnection redisConnection = new RedisConnection();
        redisConnection.testConnection();
    }
}
