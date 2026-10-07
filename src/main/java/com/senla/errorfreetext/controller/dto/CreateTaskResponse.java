package com.senla.errorfreetext.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Created task identifier")
public record CreateTaskResponse(
        @Schema(description = "Task UUID")
        UUID id
) {
}
