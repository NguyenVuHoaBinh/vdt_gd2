package Viettel.backend.AdvanceRAG.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Chunker {

    private final TokenizerService tokenizerService;

    @Autowired
    public Chunker(TokenizerService tokenizerService) {
        this.tokenizerService = tokenizerService;
    }

    // Split text into sentences
    public List<String> splitIntoSentences(String text) {
        // Use regex to split text into sentences
        String[] sentences = text.split("(?<=[.!?])\\s+");
        return Arrays.asList(sentences);
    }

    // Main method to chunk documents
    public List<Map<String, Object>> sentenceWiseTokenizedChunkDocuments(
            List<Map<String, Object>> documents,
            int chunkSize,
            int overlap,
            int minChunkSize) {

        List<Map<String, Object>> chunkedDocuments = new ArrayList<>();

        for (Map<String, Object> doc : documents) {
            String text = (String) doc.get("content"); // Adjust field name as needed
            List<String> sentences = splitIntoSentences(text);

            List<Integer> tokens = new ArrayList<>();
            List<Integer> sentenceBoundaries = new ArrayList<>();
            sentenceBoundaries.add(0);

            // Tokenize all sentences and keep track of sentence boundaries
            for (String sentence : sentences) {
                List<Integer> sentenceTokens = tokenizerService.encode(sentence);
                tokens.addAll(sentenceTokens);
                sentenceBoundaries.add(tokens.size());
            }

            // Create chunks
            int chunkStart = 0;
            while (chunkStart < tokens.size()) {
                int chunkEnd = chunkStart + chunkSize;

                // Find the last complete sentence that fits in the chunk
                int sentenceEnd = tokens.size();
                for (int boundary : sentenceBoundaries) {
                    if (boundary > chunkEnd) {
                        sentenceEnd = boundary;
                        break;
                    }
                }
                chunkEnd = Math.min(chunkEnd, sentenceEnd);

                // Create the chunk
                List<Integer> chunkTokens = new ArrayList<>(tokens.subList(chunkStart, chunkEnd));

                // Check if the chunk meets the minimum size requirement
                if (chunkTokens.size() >= minChunkSize) {
                    // Create a new document object for this chunk
                    Map<String, Object> chunkDoc = new HashMap<>();
                    chunkDoc.put("id", UUID.randomUUID().toString());
                    chunkDoc.put("chunk", chunkTokens);
                    chunkDoc.put("original_text", tokenizerService.decode(chunkTokens));
                    chunkDoc.put("chunk_index", chunkedDocuments.size());
                    chunkDoc.put("parent_id", doc.get("id"));
                    chunkDoc.put("chunk_token_count", chunkTokens.size());

                    // Copy all other fields from the original document
                    for (Map.Entry<String, Object> entry : doc.entrySet()) {
                        String key = entry.getKey();
                        if (!key.equals("content") && !chunkDoc.containsKey(key)) {
                            chunkDoc.put(key, entry.getValue());
                        }
                    }

                    chunkedDocuments.add(chunkDoc);
                }

                // Move to the next chunk start, considering overlap
                chunkStart = Math.max(chunkStart + chunkSize - overlap, chunkEnd - overlap);
            }
        }

        return chunkedDocuments;
    }
}
