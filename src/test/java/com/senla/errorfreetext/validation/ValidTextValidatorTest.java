package com.senla.errorfreetext.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidTextValidatorTest {

    private final ValidTextValidator validator = new ValidTextValidator();

    @ParameterizedTest
    @ValueSource(strings = {"abc", "hello", "Привет", "ok!", "id42text"})
    void acceptsNormalText(String text) {
        assertThat(validator.isValid(text, null)).isTrue();
    }

    @Test
    void rejectsNull() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "ab", "!!"})
    void rejectsTextShorterThanThreeCharacters(String text) {
        assertThat(validator.isValid(text, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "0000", "42"})
    void rejectsDigitsOnly(String text) {
        assertThat(validator.isValid(text, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"!!!", "@@@###", "...", "12!!"})
    void rejectsSpecialCharactersAndDigitsWithoutLetters(String text) {
        assertThat(validator.isValid(text, null)).isFalse();
    }
}
