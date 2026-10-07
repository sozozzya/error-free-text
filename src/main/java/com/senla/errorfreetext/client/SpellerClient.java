package com.senla.errorfreetext.client;

import com.senla.errorfreetext.model.Language;
import java.util.List;

public interface SpellerClient {

    List<SpellResult> checkTexts(List<String> texts, Language language, int options);
}
