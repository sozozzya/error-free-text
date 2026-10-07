package com.senla.errorfreetext.service;

import com.senla.errorfreetext.client.SpellResult;
import com.senla.errorfreetext.client.SpellerClient;
import com.senla.errorfreetext.exception.SpellerClientException;
import com.senla.errorfreetext.model.Language;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TextCorrectionService {

    private final TextChunker textChunker;
    private final SpellerOptionsCalculator optionsCalculator;
    private final SpellCorrectionApplier correctionApplier;
    private final SpellerClient spellerClient;

    public TextCorrectionService(
            TextChunker textChunker,
            SpellerOptionsCalculator optionsCalculator,
            SpellCorrectionApplier correctionApplier,
            SpellerClient spellerClient
    ) {
        this.textChunker = textChunker;
        this.optionsCalculator = optionsCalculator;
        this.correctionApplier = correctionApplier;
        this.spellerClient = spellerClient;
    }

    public String correct(String text, Language language) {
        int options = optionsCalculator.calculate(text);
        List<String> chunks = textChunker.split(text);
        StringBuilder corrected = new StringBuilder();

        for (String chunk : chunks) {
            List<SpellResult> results = spellerClient.checkTexts(List.of(chunk), language, options);
            if (results.size() != 1) {
                throw new SpellerClientException(
                        "Yandex Speller returned unexpected number of results: " + results.size()
                );
            }
            corrected.append(correctionApplier.apply(chunk, results.get(0).errors()));
        }
        return corrected.toString();
    }
}
