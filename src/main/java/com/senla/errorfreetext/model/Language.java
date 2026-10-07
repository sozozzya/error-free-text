package com.senla.errorfreetext.model;

import java.util.Locale;

public enum Language {
    RU,
    EN;

    public String toSpellerCode() {
        return name().toLowerCase(Locale.ROOT);
    }
}
