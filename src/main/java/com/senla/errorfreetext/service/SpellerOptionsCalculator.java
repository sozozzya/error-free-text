package com.senla.errorfreetext.service;

import com.senla.errorfreetext.model.SpellerConstants;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SpellerOptionsCalculator {

    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)(https?://|www\\.)\\S+");

    public int calculate(String text) {
        int options = 0;
        if (DIGIT_PATTERN.matcher(text).find()) {
            options |= SpellerConstants.IGNORE_DIGITS;
        }
        if (URL_PATTERN.matcher(text).find()) {
            options |= SpellerConstants.IGNORE_URLS;
        }
        return options;
    }
}
