package com.senla.errorfreetext.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.senla.errorfreetext.client.SpellError;
import com.senla.errorfreetext.client.SpellResult;
import com.senla.errorfreetext.client.SpellerClient;
import com.senla.errorfreetext.model.Language;
import com.senla.errorfreetext.model.SpellerConstants;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TextCorrectionServiceTest {

    @Mock
    private SpellerClient spellerClient;

    private TextCorrectionService textCorrectionService;

    @BeforeEach
    void setUp() {
        textCorrectionService = new TextCorrectionService(
                new TextChunker(),
                new SpellerOptionsCalculator(),
                new SpellCorrectionApplier(),
                spellerClient
        );
    }

    @Test
    void returnsOriginalTextWhenThereAreNoErrors() {
        String text = "correct text";
        when(spellerClient.checkTexts(List.of(text), Language.EN, 0))
                .thenReturn(List.of(new SpellResult(List.of())));

        String result = textCorrectionService.correct(text, Language.EN);

        assertThat(result).isEqualTo(text);
    }

    @Test
    void appliesSingleCorrection() {
        String text = "синхрафазатрон";
        when(spellerClient.checkTexts(List.of(text), Language.RU, 0))
                .thenReturn(List.of(new SpellResult(List.of(error(0, 14, "синхрафазатрон", List.of("синхрофазотрон"))))));

        String result = textCorrectionService.correct(text, Language.RU);

        assertThat(result).isEqualTo("синхрофазотрон");
    }

    @Test
    void appliesSeveralErrorsFromRightToLeftUsingOriginalPositions() {
        String text = "teh qick";
        when(spellerClient.checkTexts(List.of(text), Language.EN, 0)).thenReturn(List.of(new SpellResult(List.of(
                error(0, 3, "teh", List.of("the")),
                error(4, 4, "qick", List.of("quick"))
        ))));

        String result = textCorrectionService.correct(text, Language.EN);

        assertThat(result).isEqualTo("the quick");
    }

    @Test
    void skipsErrorWithoutSuggestion() {
        String text = "unknwn word";
        when(spellerClient.checkTexts(List.of(text), Language.EN, 0)).thenReturn(List.of(new SpellResult(List.of(
                error(0, 6, "unknwn", List.of()),
                error(7, 4, "word", List.of("Word"))
        ))));

        String result = textCorrectionService.correct(text, Language.EN);

        assertThat(result).isEqualTo("unknwn Word");
    }

    @Test
    void usesFirstSuggestionWhenSeveralAreProvided() {
        String text = "colour";
        when(spellerClient.checkTexts(List.of(text), Language.EN, 0))
                .thenReturn(List.of(new SpellResult(List.of(
                        error(0, 6, "colour", List.of("color", "colors"))
                ))));

        String result = textCorrectionService.correct(text, Language.EN);

        assertThat(result).isEqualTo("color");
    }

    @Test
    void sendsExactlyLimitSizedTextInOneRequest() {
        String text = "a".repeat(SpellerConstants.MAX_CHUNK_SIZE);
        when(spellerClient.checkTexts(any(), eq(Language.EN), anyInt()))
                .thenReturn(List.of(new SpellResult(List.of())));

        String result = textCorrectionService.correct(text, Language.EN);

        assertThat(result).isEqualTo(text);
        verify(spellerClient, times(1)).checkTexts(List.of(text), Language.EN, 0);
    }

    @Test
    void splitsTextLargerThanLimitAndPreservesOrder() {
        String text = "x".repeat(SpellerConstants.MAX_CHUNK_SIZE) + "\n" + "y".repeat(20);
        when(spellerClient.checkTexts(any(), eq(Language.EN), anyInt()))
                .thenAnswer(invocation -> List.of(new SpellResult(List.of())));

        String result = textCorrectionService.correct(text, Language.EN);

        assertThat(result).isEqualTo(text);
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(spellerClient, times(2)).checkTexts(captor.capture(), eq(Language.EN), eq(0));
        List<List<String>> sentChunks = captor.getAllValues();
        assertThat(sentChunks.get(0).get(0)).hasSize(SpellerConstants.MAX_CHUNK_SIZE);
        assertThat(sentChunks.get(1).get(0)).startsWith("\n");
        assertThat(sentChunks.get(0).get(0) + sentChunks.get(1).get(0)).isEqualTo(text);
    }

    @Test
    void calculatesOptionsFromFullTextBeforeCallingSpeller() {
        String text = "see https://example.com code 17";
        when(spellerClient.checkTexts(any(), eq(Language.EN), anyInt()))
                .thenReturn(List.of(new SpellResult(List.of())));

        textCorrectionService.correct(text, Language.EN);

        verify(spellerClient).checkTexts(
                List.of(text),
                Language.EN,
                SpellerConstants.IGNORE_DIGITS | SpellerConstants.IGNORE_URLS
        );
    }

    private SpellError error(int pos, int len, String word, List<String> suggestions) {
        return new SpellError(1, pos, 0, pos, len, word, suggestions);
    }
}
