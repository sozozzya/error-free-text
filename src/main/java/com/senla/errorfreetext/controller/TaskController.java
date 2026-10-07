package com.senla.errorfreetext.controller;

import com.senla.errorfreetext.controller.dto.CreateTaskRequest;
import com.senla.errorfreetext.controller.dto.CreateTaskResponse;
import com.senla.errorfreetext.controller.dto.ErrorResponse;
import com.senla.errorfreetext.controller.dto.TaskResponse;
import com.senla.errorfreetext.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "Text correction tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a text correction task")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CreateTaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        CreateTaskResponse response = taskService.create(request);
        return ResponseEntity
                .created(URI.create("/tasks/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task status and correction result")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public TaskResponse getById(@PathVariable UUID id) {
        return taskService.getById(id);
    }
}
