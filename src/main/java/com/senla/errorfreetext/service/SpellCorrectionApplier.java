package com.senla.errorfreetext.service;

import com.senla.errorfreetext.client.SpellError;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SpellCorrectionApplier {

    public String apply(String text, List<SpellError> errors) {
        if (errors == null || errors.isEmpty()) {
            return text;
        }

        List<SpellError> ordered = errors.stream()
                .sorted(Comparator.comparingInt(SpellError::pos).reversed())
                .toList();

        StringBuilder result = new StringBuilder(text);
        for (SpellError error : ordered) {
            List<String> suggestions = error.suggestions();
            if (suggestions.isEmpty()) {
                continue;
            }
            int from = error.pos();
            int to = from + error.len();
            if (from < 0 || error.len() < 0 || to > result.length()) {
                continue;
            }
            result.replace(from, to, suggestions.get(0));
        }
        return result.toString();
    }
}
