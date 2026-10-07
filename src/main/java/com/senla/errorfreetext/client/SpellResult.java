package com.senla.errorfreetext.client;

import java.util.List;

public record SpellResult(List<SpellError> errors) {

    public List<SpellError> errors() {
        return errors == null ? List.of() : errors;
    }
}
