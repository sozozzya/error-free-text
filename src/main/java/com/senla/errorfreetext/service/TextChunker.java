package com.senla.errorfreetext.service;

import com.senla.errorfreetext.model.SpellerConstants;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TextChunker {

    private final int maxChunkSize;

    public TextChunker() {
        this(SpellerConstants.MAX_CHUNK_SIZE);
    }

    public TextChunker(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public List<String> split(String text) {
        if (text.length() <= maxChunkSize) {
            return List.of(text);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int remaining = text.length() - start;
            if (remaining <= maxChunkSize) {
                chunks.add(text.substring(start));
                break;
            }
            int splitAt = findSplitIndex(text, start);
            chunks.add(text.substring(start, splitAt));
            start = splitAt;
        }
        return chunks;
    }

    private int findSplitIndex(String text, int start) {
        int hardEnd = start + maxChunkSize;
        for (int i = hardEnd - 1; i > start; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i + 1;
            }
        }
        return hardEnd;
    }
}
