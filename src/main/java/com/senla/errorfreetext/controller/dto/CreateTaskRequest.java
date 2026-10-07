package com.senla.errorfreetext.controller.dto;

import com.senla.errorfreetext.model.Language;
import com.senla.errorfreetext.validation.ValidText;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create a text correction task")
public record CreateTaskRequest(
        @ValidText
        @Schema(description = "Text to correct", example = "синхрафазатрон")
        String text,

        @NotNull(message = "language must be RU or EN")
        @Schema(description = "Text language", example = "RU", allowableValues = {"RU", "EN"})
        Language language
) {
}
