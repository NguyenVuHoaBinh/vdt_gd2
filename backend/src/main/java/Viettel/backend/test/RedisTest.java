package Viettel.backend.test;

import redis.clients.jedis.Jedis;

public class RedisTest {
    public static void main(String[] args) {
        try (Jedis jedis = new Jedis("localhost", 6371)) {
            jedis.auth("hoabinh12");
            System.out.println("Connection successful: " + jedis.ping());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
