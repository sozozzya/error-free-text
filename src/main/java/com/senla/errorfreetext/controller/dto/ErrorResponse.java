package com.senla.errorfreetext.controller.dto;

import java.time.Instant;

public record ErrorResponse(
        String errorMessage,
        int errorCode,
        Instant timestamp,
        String path
) {
}
