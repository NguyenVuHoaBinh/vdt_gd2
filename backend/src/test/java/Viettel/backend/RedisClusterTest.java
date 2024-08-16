package Viettel.backend;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
@SpringBootTest
public class RedisClusterTest {

    private JedisCluster jedisCluster;
    private Set<HostAndPort> clusterNodes;

    @Before
    public void setUp() {
        clusterNodes = new HashSet<>();
        clusterNodes.add(new HostAndPort("localhost", 6371));
        clusterNodes.add(new HostAndPort("localhost", 6372));
        clusterNodes.add(new HostAndPort("localhost", 6373));
        clusterNodes.add(new HostAndPort("localhost", 6374));
        clusterNodes.add(new HostAndPort("localhost", 6375));
        clusterNodes.add(new HostAndPort("localhost", 6376));

        jedisCluster = new JedisCluster(clusterNodes, 5000, 5000, 3, "hoabinh12", null);
    }

    @Test
    public void testClusterConnection() {
        clusterNodes.forEach(node -> {
            try (var jedis = new Jedis(node.getHost(), node.getPort())) {
                String response = jedis.ping();
                assertTrue("Connection to node " + node + " failed.", "PONG".equals(response));
                System.out.println("Node " + node + " is reachable with response: " + response);
            } catch (Exception e) {
                System.err.println("Failed to connect to node " + node + ": " + e.getMessage());
            }
        });
    }

    @After
    public void tearDown() {
        if (jedisCluster != null) {
            try {
                jedisCluster.close();
            } catch (Exception e) {
                System.out.println("Error while closing JedisCluster: " + e.getMessage());
            }
        }
    }
}
