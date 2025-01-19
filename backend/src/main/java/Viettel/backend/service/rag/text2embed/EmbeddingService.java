package Viettel.backend.service.rag.text2embed;

import Viettel.backend.model.ChatDocument;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.stream.Collectors;

@Service
public interface EmbeddingService {
    List<float[]> embed(List<String> texts);

    default float[] embedText(String text) {
        return embed(List.of(text)).get(0);
    }

    default List<float[]> embedDocuments(List<ChatDocument> chatDocuments) {
        List<String> texts = chatDocuments.stream()
                .map(ChatDocument::getText)
                .collect(Collectors.toList());
        return embed(texts);
    }


    default byte[] floatArrayToByteArray(float[] input) {
        // https://redis.io/kb/doc/13qsrk8xpx/how-to-perform-vector-search-in-java-with-the-jedis-client-library
        byte[] bytes = new byte[Float.BYTES * input.length];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(input);
        return bytes;
    }
}
