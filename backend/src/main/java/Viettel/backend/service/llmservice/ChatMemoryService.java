package Viettel.backend.service.llmservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.AbstractTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Service for managing chat memory, including session metadata and chat history.
 */
@Service
public class ChatMemoryService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMemoryService.class);

    // Distinct prefixes for metadata and chat history
    private static final String SESSION_METADATA_PREFIX = "session:metadata:";
    private static final String CHAT_HISTORY_PREFIX = "session:chat:";

    // Expiration times in seconds
    private static final int METADATA_EXPIRATION = 24 * 3600; // 24 hours
    private static final int CHAT_HISTORY_EXPIRATION = 7 * 24 * 3600; // 7 days

    private final JedisPooled jedisPooled;


    /**
     * Constructor for dependency injection.
     *
     * @param jedisPooled the JedisPooled instance for Redis operations
     */
    @Autowired
    public ChatMemoryService(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
    }

    /**
     * Stores session metadata in Redis.
     *
     * @param sessionId the unique identifier for the session
     * @param metadata  a map of metadata key-value pairs
     */
    public void storeSessionMetadata(String sessionId, Map<String, String> metadata) {
        if (!isValidSessionId(sessionId) || metadata == null) {
            logger.warn("Invalid sessionId or metadata provided for storing session metadata. sessionId: {}", maskSessionId(sessionId));
            return;
        }

        String redisKey = SESSION_METADATA_PREFIX + sessionId;
        try {
            logger.debug("Storing session metadata for sessionId: {}, redisKey: {}", maskSessionId(sessionId), redisKey);
            AbstractTransaction transaction = jedisPooled.multi();
            transaction.hset(redisKey, metadata);
            transaction.expire(redisKey, METADATA_EXPIRATION);
            transaction.exec();
            logger.info("Stored session metadata for sessionId: {}", maskSessionId(sessionId));
        } catch (JedisException e) {
            logger.error("Failed to store session metadata for sessionId: {}", maskSessionId(sessionId), e);
        }
    }

    /**
     * Stores a user chat message in Redis.
     *
     * @param sessionId the unique identifier for the session
     * @param userType  the type of user (e.g., "user", "assistant")
     * @param message   the chat message content
     */
    public void storeUserChat(String sessionId, String userType, String message) {
        if (!isValidSessionId(sessionId) || !isValidUserType(userType) || !isValidMessage(message)) {
            logger.warn("Invalid input provided for storing user chat. sessionId: {}, userType: {}", maskSessionId(sessionId), userType);
            return;
        }

        String redisKey = CHAT_HISTORY_PREFIX + sessionId;
        String formattedMessage = formatMessage(userType, message);
        try {
            logger.debug("Storing chat message for sessionId: {}, userType: {}, redisKey: {}", maskSessionId(sessionId), userType, redisKey);
            AbstractTransaction transaction = jedisPooled.multi();
            transaction.rpush(redisKey, formattedMessage);
            transaction.expire(redisKey, CHAT_HISTORY_EXPIRATION);
            transaction.exec();
            logger.info("Stored chat message for sessionId: {}, userType: {}", maskSessionId(sessionId), userType);
        } catch (JedisException e) {
            logger.error("Failed to store chat message for sessionId: {}, userType: {}", maskSessionId(sessionId), userType, e);
        }
    }

    /**
     * Retrieves session metadata from Redis.
     *
     * @param sessionId the unique identifier for the session
     * @return a map of metadata key-value pairs, or an empty map if not found or on error
     */
    public Map<String, String> getSessionMetadata(String sessionId) {
        if (!isValidSessionId(sessionId)) {
            logger.warn("Invalid sessionId provided for retrieving session metadata. sessionId: {}", maskSessionId(sessionId));
            return new HashMap<>();
        }

        String redisKey = SESSION_METADATA_PREFIX + sessionId;
        try {
            logger.debug("Retrieving session metadata for sessionId: {}, redisKey: {}", maskSessionId(sessionId), redisKey);
            Map<String, String> metadata = jedisPooled.hgetAll(redisKey);
            if (metadata == null || metadata.isEmpty()) {
                logger.info("No session metadata found for sessionId: {}", maskSessionId(sessionId));
                return new HashMap<>();
            }
            return metadata;
        } catch (JedisException e) {
            logger.error("Failed to retrieve session metadata for sessionId: {}", maskSessionId(sessionId), e);
            return new HashMap<>();
        }
    }

    /**
     * Retrieves user chat history from Redis.
     *
     * @param sessionId the unique identifier for the session
     * @return a list of formatted chat messages, or an empty list if not found or on error
     */
    public List<String> getUserChat(String sessionId) {
        if (!isValidSessionId(sessionId)) {
            logger.warn("Invalid sessionId provided for retrieving user chat. sessionId: {}", maskSessionId(sessionId));
            return new ArrayList<>();
        }

        String redisKey = CHAT_HISTORY_PREFIX + sessionId;
        try {
            logger.debug("Retrieving chat history for sessionId: {}, redisKey: {}", maskSessionId(sessionId), redisKey);
            List<String> chatHistory = jedisPooled.lrange(redisKey, 0, -1);
            if (chatHistory == null || chatHistory.isEmpty()) {
                logger.info("No chat history found for sessionId: {}", maskSessionId(sessionId));
                return new ArrayList<>();
            }
            return chatHistory;
        } catch (JedisException e) {
            logger.error("Failed to retrieve chat history for sessionId: {}", maskSessionId(sessionId), e);
            return new ArrayList<>();
        }
    }

    /**
     * Initializes a new session with metadata and sets the appropriate expirations.
     * This method should be called once when a new session is created.
     *
     * @param sessionId the unique identifier for the session
     * @param metadata  a map of metadata key-value pairs
     */
    public void initializeSession(String sessionId, Map<String, String> metadata) {
        if (!isValidSessionId(sessionId) || metadata == null) {
            logger.warn("Invalid sessionId or metadata provided for initializing session. sessionId: {}", maskSessionId(sessionId));
            return;
        }

        String metadataKey = SESSION_METADATA_PREFIX + sessionId;
        String chatKey = CHAT_HISTORY_PREFIX + sessionId;
        try {
            logger.debug("Initializing session for sessionId: {}, metadataKey: {}, chatKey: {}", maskSessionId(sessionId), metadataKey, chatKey);
            AbstractTransaction transaction = jedisPooled.multi();
            transaction.hset(metadataKey, metadata);
            transaction.expire(metadataKey, METADATA_EXPIRATION);
            transaction.expire(chatKey, CHAT_HISTORY_EXPIRATION);
            transaction.exec();
            logger.info("Initialized new session with sessionId: {}", maskSessionId(sessionId));
        } catch (JedisException e) {
            logger.error("Failed to initialize session with sessionId: {}", maskSessionId(sessionId), e);
        }
    }

    /**
     * Formats a chat message with user type and timestamp.
     *
     * @param userType the type of user
     * @param message  the chat message content
     * @return a formatted string containing user type, timestamp, and message
     */
    private String formatMessage(String userType, String message) {
        long timestamp = System.currentTimeMillis();
        // Example format: "user:1638316800000:Hello, how are you?"
        return String.format("%s:%d:%s", userType, timestamp, message);
    }

    /**
     * Validates the session ID.
     *
     * @param sessionId the session ID to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidSessionId(String sessionId) {
        return sessionId != null && !sessionId.trim().isEmpty();
    }

    /**
     * Validates the user type.
     *
     * @param userType the user type to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidUserType(String userType) {
        return userType != null && (userType.equals("user") || userType.equals("assistant"));
    }

    /**
     * Validates the message content.
     *
     * @param message the message to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidMessage(String message) {
        return message != null && !message.trim().isEmpty();
    }

    /**
     * Masks the session ID for logging to prevent exposing sensitive information.
     *
     * @param sessionId the original session ID
     * @return a masked version of the session ID
     */
    private String maskSessionId(String sessionId) {
        if (sessionId.length() <= 4) {
            return "****";
        }
        String visiblePart = sessionId.substring(sessionId.length() - 4);
        return "****" + visiblePart;
    }

    /**
     * Stores a message or entity data (order, invoice, etc.) in Redis by entity type.
     *
     * @param sessionId  the unique identifier for the session
     * @param entityType the type of entity (e.g., "chat", "order", "invoice")
     * @param data       the data content to store
     */
    public void storeEntityData(String sessionId, String entityType, String data) {
        if (!isValidSessionId(sessionId) || !isValidEntityType(entityType)) {
            logger.warn("Invalid input provided for storing entity data. sessionId: {}, entityType: {}", maskSessionId(sessionId), entityType);
            return;
        }

        String redisKey = entityType + ":" + sessionId;
        try {
            logger.debug("Storing data for sessionId: {}, entityType: {}, redisKey: {}", maskSessionId(sessionId), entityType, redisKey);
            AbstractTransaction transaction = jedisPooled.multi();
            transaction.del(redisKey);
            transaction.rpush(redisKey, data);
            transaction.expire(redisKey, getExpirationTime(entityType));
            transaction.exec();
            logger.info("Stored data for sessionId: {}, entityType: {}", maskSessionId(sessionId), entityType);
        } catch (JedisException e) {
            logger.error("Failed to store data for sessionId: {}, entityType: {}", maskSessionId(sessionId), entityType, e);
        }
    }

    /**
     * Retrieves entity data (chat history, order details, etc.) from Redis by entity type and session ID.
     *
     * @param sessionId  the unique identifier for the session
     * @param entityType the type of entity (e.g., "chat", "order", "invoice")
     * @return a list of data entries or an empty list if not found or on error
     */
    public List<String> getEntityData(String sessionId, String entityType) {
        if (!isValidSessionId(sessionId) || !isValidEntityType(entityType)) {
            logger.warn("Invalid sessionId or entityType provided for retrieving data. sessionId: {}, entityType: {}", maskSessionId(sessionId), entityType);
            return new ArrayList<>();
        }

        String redisKey = entityType + ":" + sessionId;
        try {
            logger.debug("Retrieving data for sessionId: {}, entityType: {}, redisKey: {}", maskSessionId(sessionId), entityType, redisKey);
            List<String> dataEntries = jedisPooled.lrange(redisKey, 0, -1);
            if (dataEntries == null || dataEntries.isEmpty()) {
                logger.info("No data found for sessionId: {}, entityType: {}", maskSessionId(sessionId), entityType);
                return new ArrayList<>();
            }
            return dataEntries;
        } catch (JedisException e) {
            logger.error("Failed to retrieve data for sessionId: {}, entityType: {}", maskSessionId(sessionId), entityType, e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets the expiration time based on entity type.
     *
     * @param entityType the type of entity
     * @return the expiration time in seconds
     */
    private int getExpirationTime(String entityType) {
        switch (entityType) {
            case "metadata":
                return METADATA_EXPIRATION;
            case "chat":
                return CHAT_HISTORY_EXPIRATION;
            case "order":
            case "invoice":
                return 30 * 24 * 3600; // 30 days, for example
            default:
                return 7 * 24 * 3600; // Default to 7 days if unspecified
        }
    }

    /**
     * Validates the entity type.
     *
     * @param entityType the entity type to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEntityType(String entityType) {
        return entityType != null && (entityType.equals("chat") ||
                entityType.equals("order") ||
                entityType.equals("invoice") ||
                entityType.equals("metadata") ||
                entityType.equals("customer")) ||
                entityType.equals("invoice") ||
                entityType.equals("invoice_id") ||
                entityType.equals("product_info") ||
                entityType.equals("check_debt_info") ||
                entityType.equals("json") ||
                entityType.equals("jsonImport") ||
                entityType.equals("jsonAdjust") ||
                entityType.equals("jsonBA") ||
                entityType.equals("jsonDebt") ||
                entityType.equals("jsonInactive") ||
                entityType.equals("jsonRestockAlert") ||
                entityType.equals("jsonTrend")  ||
                entityType.equals("jsonAmbiguousDetection") ||
                entityType.equals("finalSearch")    ||
                entityType.equals("finalBook")  ||
                entityType.equals("finalPlaying")
                ;
    }




}
