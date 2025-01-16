package Viettel.backend.service.chatmemory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.Schema.VectorField;
import redis.clients.jedis.search.Schema.VectorField.VectorAlgo;

import java.util.HashMap;
import java.util.Map;
// https://javadoc.io/doc/redis.clients/jedis/4.2.3/redis/clients/jedis/search/Schema.VectorField.html
// https://redis.io/kb/doc/13qsrk8xpx/how-to-perform-vector-search-in-java-with-the-jedis-client-library
@Service
public class RedisService {
    // Distinct prefixes for metadata and chat history
    private static final String SESSION_METADATA_PREFIX = "session:metadata:";
    private static final String CHAT_HISTORY_PREFIX = "session:chat:";

    // Expiration times in seconds
    private static final int METADATA_EXPIRATION = 24 * 3600; // 24 hours
    private static final int CHAT_HISTORY_EXPIRATION = 7 * 24 * 3600; // 7 days

    private final JedisPooled jedisPooled;

    private static final String INDEX_NAME = "chatMemoryIndex";
    private static final String VECTOR_FIELD_NAME = "chatMemoryEmbedding";
    private static final String TEXT_FIELD_NAME = "text";
    private static final String TAG_FIELD_NAME = "conversation_id";

    private static final String VECTOR_TYPE = "FLOAT32";
    private static final int VECTOR_DIMENSIONS = 1024;
    private static final String DISTANCE_METRIC = "L2";
    private static final int NUM_VECTORS = 686;
    private static final int M = 40;
    private static final int EF_CONSTRUCTION = 200;

    @Autowired
    public RedisService(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
    }

    public void createIndex() {
//        https://redis.io/docs/latest/develop/interact/search-and-query/advanced-concepts/vectors/
        VectorAlgo algorithm = VectorAlgo.HNSW;
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("TYPE", VECTOR_TYPE);
        attributes.put("DIM", VECTOR_DIMENSIONS);
        attributes.put("DISTANCE_METRIC", DISTANCE_METRIC);
        attributes.put("M", M);
        attributes.put("EF_CONSTRUCTION", EF_CONSTRUCTION);

        Schema schema = new Schema()
                .addField(new VectorField(
                        VECTOR_FIELD_NAME,
                        algorithm,
                        attributes)
                .addField(TEXT_FIELD_NAME, FieldType.TEXT)
                .addField(TAG_FIELD_NAME, FieldType.TAG);


    }
}
