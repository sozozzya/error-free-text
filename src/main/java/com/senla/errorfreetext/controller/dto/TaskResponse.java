package com.senla.errorfreetext.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.senla.errorfreetext.entity.Task;
import com.senla.errorfreetext.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Task processing result")
public record TaskResponse(
        TaskStatus status,
        String text,
        String error
) {

    public static TaskResponse from(Task task) {
        return switch (task.getStatus()) {
            case COMPLETED -> new TaskResponse(task.getStatus(), task.getCorrectedText(), null);
            case FAILED -> new TaskResponse(task.getStatus(), null, task.getErrorMessage());
            case NEW, PROCESSING -> new TaskResponse(task.getStatus(), null, null);
        };
    }
}
