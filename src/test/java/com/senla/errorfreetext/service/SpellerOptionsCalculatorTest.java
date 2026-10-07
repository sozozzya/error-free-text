package com.senla.errorfreetext.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.senla.errorfreetext.model.SpellerConstants;
import org.junit.jupiter.api.Test;

class SpellerOptionsCalculatorTest {

    private final SpellerOptionsCalculator calculator = new SpellerOptionsCalculator();

    @Test
    void returnsZeroWhenTextHasNoDigitsOrUrls() {
        int options = calculator.calculate("просто текст без ссылок");

        assertThat(options).isZero();
        assertDisabledFlags(options);
    }

    @Test
    void enablesIgnoreDigitsWhenTextContainsDigits() {
        int options = calculator.calculate("код авп17х4534");

        assertThat(options).isEqualTo(SpellerConstants.IGNORE_DIGITS);
        assertDisabledFlags(options);
    }

    @Test
    void enablesIgnoreUrlsForHttpHttpsAndWww() {
        assertThat(calculator.calculate("see http://example.com")).isEqualTo(SpellerConstants.IGNORE_URLS);
        assertThat(calculator.calculate("see https://example.com/path")).isEqualTo(SpellerConstants.IGNORE_URLS);
        assertThat(calculator.calculate("see www.example.com")).isEqualTo(SpellerConstants.IGNORE_URLS);
        assertDisabledFlags(calculator.calculate("see https://example.com"));
    }

    @Test
    void combinesDigitsAndUrlOptions() {
        int options = calculator.calculate("id 42 at https://example.com");

        assertThat(options).isEqualTo(SpellerConstants.IGNORE_DIGITS | SpellerConstants.IGNORE_URLS);
        assertThat(options).isEqualTo(6);
        assertDisabledFlags(options);
    }

    private void assertDisabledFlags(int options) {
        assertThat(options & SpellerConstants.FIND_REPEAT_WORDS).isZero();
        assertThat(options & SpellerConstants.IGNORE_CAPITALIZATION).isZero();
    }
}
