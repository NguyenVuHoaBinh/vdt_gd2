package Viettel.backend;

import Viettel.backend.service.llmservice.ChatMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.JedisPooled;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatMemoryServiceTest {

    @Mock
    private JedisPooled jedisPooled;

    @InjectMocks
    private ChatMemoryService chatMemoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testStoreSessionMetadata() {
        String sessionId = "session123";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("user", "John Doe");
        metadata.put("role", "admin");

        String expectedKey = "session:" + sessionId + ":metadata";

        chatMemoryService.storeSessionMetadata(sessionId, metadata);

        verify(jedisPooled, times(1)).hset(expectedKey, metadata);
    }

    @Test
    void testGetSessionMetadata() {
        String sessionId = "session123";
        String redisKey = "session:" + sessionId + ":metadata";

        Map<String, String> expectedMetadata = new HashMap<>();
        expectedMetadata.put("user", "John Doe");
        expectedMetadata.put("role", "admin");

        when(jedisPooled.hgetAll(redisKey)).thenReturn(expectedMetadata);

        Map<String, String> actualMetadata = chatMemoryService.getSessionMetadata(sessionId);

        assertEquals(expectedMetadata, actualMetadata, "Retrieved metadata should match expected metadata");
    }

    @Test
    void testStoreUserChat() {
        String sessionId = "session123";
        String userType = "User";
        String message = "Hello, how can I help you?";

        String redisKey = "session:" + sessionId + ":chat";
        String messageKey = userType + ":" + System.currentTimeMillis();

        // Mock time to control the timestamp used in the message key
        long mockTimestamp = 1652486400000L;
        when(System.currentTimeMillis()).thenReturn(mockTimestamp);
        messageKey = userType + ":" + mockTimestamp;

        chatMemoryService.storeUserChat(sessionId, userType, message);

        verify(jedisPooled, times(1)).hset(eq(redisKey), eq(messageKey), eq(message));
    }

    @Test
    void testGetUserChat() {
        String sessionId = "session123";
        String redisKey = "session:" + sessionId + ":chat";

        Map<String, String> expectedChatHistory = new HashMap<>();
        expectedChatHistory.put("User:1652486400000", "Hello, how can I help you?");
        expectedChatHistory.put("System:1652486410000", "What do you need assistance with?");

        when(jedisPooled.hgetAll(redisKey)).thenReturn(expectedChatHistory);

        Map<String, String> actualChatHistory = chatMemoryService.getUserChat(sessionId);

        assertEquals(expectedChatHistory, actualChatHistory, "Retrieved chat history should match expected chat history");
    }
}
