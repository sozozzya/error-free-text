package com.senla.errorfreetext.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.senla.errorfreetext.controller.dto.CreateTaskRequest;
import com.senla.errorfreetext.controller.dto.CreateTaskResponse;
import com.senla.errorfreetext.exception.ErrorCodes;
import com.senla.errorfreetext.exception.TaskNotFoundException;
import com.senla.errorfreetext.service.TaskService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createsTaskForValidRuRequest() throws Exception {
        UUID id = UUID.fromString("44bd78dc-d08c-41c6-b87d-fb82046bd470");
        when(taskService.create(any(CreateTaskRequest.class))).thenReturn(new CreateTaskResponse(id));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"привет мир","language":"RU"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void createsTaskForValidEnRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(taskService.create(any(CreateTaskRequest.class))).thenReturn(new CreateTaskResponse(id));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"hello world","language":"EN"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsUnknownLanguage() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"hello world","language":"FR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.VALIDATION))
                .andExpect(jsonPath("$.errorMessage").value("language must be RU or EN"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(taskService, never()).create(any());
    }

    @Test
    void rejectsTooShortText() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"ab","language":"EN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.VALIDATION));
    }

    @Test
    void rejectsDigitsOnly() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"12345","language":"EN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.VALIDATION));
    }

    @Test
    void rejectsSpecialCharactersOnly() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"!!!???","language":"RU"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.VALIDATION));
    }

    @Test
    void returnsNotFoundErrorPayload() throws Exception {
        UUID id = UUID.fromString("44bd78dc-d08c-41c6-b87d-fb82046bd470");
        when(taskService.getById(id)).thenThrow(new TaskNotFoundException(id));

        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Task with id: 44bd78dc-d08c-41c6-b87d-fb82046bd470 not found"))
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.TASK_NOT_FOUND))
                .andExpect(jsonPath("$.path").value("/tasks/" + id))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
