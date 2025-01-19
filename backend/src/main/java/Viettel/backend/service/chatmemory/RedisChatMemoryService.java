package Viettel.backend.service.chatmemory;

import Viettel.backend.model.ChatDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.AbstractPipeline;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.*;

import java.util.*;
import java.util.stream.Collectors;

// cmd: https://redis.io/docs/latest/commands/
// redis chat-memory-project by redis
//      https://redis.io/blog/chatgpt-memory-project/
//      https://github.com/continuum-llms/chatgpt-memory
// develop with redis > interact with data
//      https://redis.io/docs/latest/develop/interact/search-and-query/advanced-concepts/vectors/
// jedis (pooled) (java)
//      https://redis.io/docs/latest/develop/clients/jedis/
//      https://javadoc.io/doc/redis.clients/jedis/latest/index.html

// https://redis.io/kb/doc/13qsrk8xpx/how-to-perform-vector-search-in-java-with-the-jedis-client-library


@Service
public class RedisChatMemoryService {
    // Distinct prefixes for metadata and chat history
    private static final String SESSION_METADATA_PREFIX = "session:metadata:";
    private static final String CHAT_HISTORY_PREFIX = "session:chat:";

    // Expiration times in seconds
    private static final int METADATA_EXPIRATION = 24 * 3600; // 24 hours
    private static final int CHAT_HISTORY_EXPIRATION = 7 * 24 * 3600; // 7 days

    private static final String INDEX_NAME = "chatMemoryIndex";
    private static final String VECTOR_FIELD_NAME = "embedding";
    private static final String TEXT_FIELD_NAME = "text";
    private static final String TAG_FIELD_NAME = "sessionId";
    private static final String ID_FIELD_NAME = "id"; // default field "id" of Redis

    private static final String VECTOR_TYPE = "FLOAT32";
    private static final int VECTOR_DIMENSIONS = 1536; // based on openai byte[] embedding.length
    private static final String DISTANCE_METRIC = "L2";
    private static final int M = 40;
    private static final int EF_CONSTRUCTION = 200;
    private static final int INITIAL_CAP = 686;
    // beside noted fields, the rest is based on listed articles above in chatmemory project and vector search guide

    private final JedisPooled jedisPooled;
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisChatMemoryService(JedisPooled jedisPooled, ObjectMapper objectMapper) {
        this.jedisPooled = jedisPooled;
        this.objectMapper = objectMapper;
    }

    public void createIndex() {
        Map<String, Object> attr = new HashMap<>();
        attr.put("TYPE", VECTOR_TYPE);
        attr.put("DIM", VECTOR_DIMENSIONS);
        attr.put("DISTANCE_METRIC", DISTANCE_METRIC);
        attr.put("M", M);
        attr.put("EF_CONSTRUCTION", EF_CONSTRUCTION);
        attr.put("INITIAL_CAP", INITIAL_CAP);

        Schema schema = new Schema()
                .addHNSWVectorField(VECTOR_FIELD_NAME, attr)
                .addTextField(TEXT_FIELD_NAME, 1)
                .addTagField(TAG_FIELD_NAME);

        IndexDefinition definition = new IndexDefinition().setPrefixes(CHAT_HISTORY_PREFIX);
        IndexOptions options = IndexOptions.defaultOptions().setDefinition(definition);

        try {
            jedisPooled.ftCreate(INDEX_NAME, options, schema);
        } catch (JedisDataException e) {
            System.err.println(e.getMessage());
        }
    }

    public void indexDocuments(List<ChatDocument> chatDocuments) {
        // chat documents: list of documents to be indexed

        AbstractPipeline pipe = jedisPooled.pipelined();
        for (ChatDocument chatDocument : chatDocuments) {
            // uuid instead of sessionID to prevent overriding when storing multiple documents with same sessionId
            String key = CHAT_HISTORY_PREFIX + UUID.randomUUID();
            Map<String, String> hash = objectMapper.convertValue(chatDocument, Map.class);
            System.out.println("34"+hash);
            System.out.println("12"+key);
            pipe.hset(key, hash);
            pipe.hset(key.getBytes(), VECTOR_FIELD_NAME.getBytes(), chatDocument.getEmbedding());
        }
        pipe.sync();
    }

    public List<Document> searchDocuments(byte[] queryVector, String sessionId, int topK) {
        // filter criteria: exact match for {sessionId}
        String primaryFilterQuery = String.format("(@%s:{%s})", TAG_FIELD_NAME, sessionId);
        // query vector (embedding)
        // sortby ascending=true
        // The lower the score, the more similar the document is to the query, as we use L2 (Euclidean distance).
        Query query = new Query(String.format(
                "%s=>[KNN %d @%s $BLOB AS score]", primaryFilterQuery, topK, VECTOR_FIELD_NAME))
                .returnFields(TAG_FIELD_NAME, TEXT_FIELD_NAME, "score")
                .addParam("BLOB", queryVector)
                .setSortBy("score", true)
                .limit(0, topK)
                .dialect(2);

        SearchResult searchResult = jedisPooled.ftSearch(INDEX_NAME, query);

        return searchResult.getDocuments();
    }

    public List<String> getAllSessionIds() {
        // TODO: contain dups, make unique
        Query query = new Query("*").returnFields(TAG_FIELD_NAME);
        SearchResult searchResult = jedisPooled.ftSearch(INDEX_NAME, query);

        return searchResult.getDocuments().stream()
                .map(doc -> doc.getString(TAG_FIELD_NAME))
                .collect(Collectors.toList());
    }

    public void deleteDocuments(String sessionId) {
        String primaryFilterQuery = String.format("(@%s:{%s})", TAG_FIELD_NAME, sessionId);
        Query query = new Query(primaryFilterQuery)
                .returnFields(ID_FIELD_NAME)
                .dialect(2);

        SearchResult searchResult = jedisPooled.ftSearch(INDEX_NAME, query);

        for (Document document : searchResult.getDocuments()) {
            jedisPooled.del(INDEX_NAME, document.getId());
        }

    }
}
