package Viettel.backend.AdvanceRAG.service;

import org.springframework.stereotype.Service;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;

import java.util.ArrayList;
import java.util.List;

@Service
public class TokenizerService {

    private final Encoding encoding;

    public TokenizerService() {
        // Initialize the encoding with cl100k_base (used by OpenAI models like text-embedding-ada-002)
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        this.encoding = registry.getEncoding(EncodingType.CL100K_BASE);
    }

    // Encode text into List<Integer>
    public List<Integer> encode(String text) {
        IntArrayList tokens = encoding.encode(text);
        // Manually convert IntArrayList to List<Integer>
        List<Integer> tokenList = new ArrayList<>(tokens.size());
        for (int i = 0; i < tokens.size(); i++) {
            tokenList.add(tokens.get(i));
        }
        return tokenList;
    }

    // Decode List<Integer> back into text
    public String decode(List<Integer> tokens) {
        // Convert List<Integer> to IntArrayList
        IntArrayList intArrayTokens = new IntArrayList(tokens.size());
        for (Integer token : tokens) {
            intArrayTokens.add(token);
        }
        return encoding.decode(intArrayTokens);
    }

    // Count the number of tokens in the text
    public int countTokens(String text) {
        return encoding.countTokens(text);
    }
}
