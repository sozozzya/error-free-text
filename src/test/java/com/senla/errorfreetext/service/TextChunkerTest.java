package com.senla.errorfreetext.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.senla.errorfreetext.model.SpellerConstants;
import java.util.List;
import org.junit.jupiter.api.Test;

class TextChunkerTest {

    private final TextChunker defaultChunker = new TextChunker();

    @Test
    void doesNotSplitTextShorterThanLimit() {
        String text = "hello world";

        assertThat(defaultChunker.split(text)).containsExactly(text);
    }

    @Test
    void doesNotSplitTextExactlyAtLimit() {
        String text = "a".repeat(SpellerConstants.MAX_CHUNK_SIZE);

        List<String> chunks = defaultChunker.split(text);

        assertThat(chunks).containsExactly(text);
        assertThat(chunks.get(0)).hasSize(SpellerConstants.MAX_CHUNK_SIZE);
    }

    @Test
    void splitsOnWhitespaceAndPreservesSpacesAndNewlines() {
        TextChunker chunker = new TextChunker(10);
        String text = "hello\n\nworld extra";

        List<String> chunks = chunker.split(text);

        assertThat(String.join("", chunks)).isEqualTo(text);
        assertThat(chunks).allMatch(chunk -> chunk.length() <= 10);
        assertThat(text).contains("\n\n");
        assertThat(String.join("", chunks)).contains("\n\n");
    }

    @Test
    void hardSplitsWordLongerThanLimit() {
        TextChunker chunker = new TextChunker(10);
        String word = "a".repeat(12);

        List<String> chunks = chunker.split(word);

        assertThat(chunks).containsExactly("a".repeat(10), "aa");
        assertThat(String.join("", chunks)).isEqualTo(word);
    }

    @Test
    void keepsChunkOrderForLongText() {
        String first = "one ".repeat(2000);
        String second = "two ".repeat(2000);
        String text = first + second;
        assertThat(text.length()).isGreaterThan(SpellerConstants.MAX_CHUNK_SIZE);

        List<String> chunks = defaultChunker.split(text);

        assertThat(chunks.size()).isGreaterThan(1);
        assertThat(chunks.get(0)).startsWith("one ");
        assertThat(String.join("", chunks)).isEqualTo(text);
        assertThat(chunks).allMatch(chunk -> chunk.length() <= SpellerConstants.MAX_CHUNK_SIZE);
    }
}
