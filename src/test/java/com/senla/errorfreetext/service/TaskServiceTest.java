package com.senla.errorfreetext.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.senla.errorfreetext.controller.dto.CreateTaskRequest;
import com.senla.errorfreetext.controller.dto.CreateTaskResponse;
import com.senla.errorfreetext.controller.dto.TaskResponse;
import com.senla.errorfreetext.entity.Task;
import com.senla.errorfreetext.exception.TaskNotFoundException;
import com.senla.errorfreetext.model.Language;
import com.senla.errorfreetext.model.TaskStatus;
import com.senla.errorfreetext.repository.TaskRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);
    }

    @Test
    void createPersistsNewTaskAndReturnsId() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CreateTaskRequest request = new CreateTaskRequest("hello world", Language.EN);

        CreateTaskResponse response = taskService.create(request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(saved.getText()).isEqualTo("hello world");
        assertThat(saved.getLanguage()).isEqualTo(Language.EN);
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.NEW);
        assertThat(saved.getCorrectedText()).isNull();
    }

    @Test
    void getByIdReturnsOnlyStatusForNewTask() {
        Task task = Task.create("hello world", Language.RU);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getById(task.getId());

        assertThat(response.status()).isEqualTo(TaskStatus.NEW);
        assertThat(response.text()).isNull();
        assertThat(response.error()).isNull();
    }

    @Test
    void getByIdReturnsOnlyStatusForProcessingTask() {
        Task task = Task.create("hello world", Language.RU);
        task.markProcessing();
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getById(task.getId());

        assertThat(response).isEqualTo(new TaskResponse(TaskStatus.PROCESSING, null, null));
    }

    @Test
    void getByIdReturnsCorrectedTextForCompletedTask() {
        Task task = Task.create("teh text", Language.EN);
        task.markProcessing();
        task.complete("the text");
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getById(task.getId());

        assertThat(response.status()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(response.text()).isEqualTo("the text");
        assertThat(response.error()).isNull();
    }

    @Test
    void getByIdReturnsErrorForFailedTask() {
        Task task = Task.create("hello world", Language.EN);
        task.markProcessing();
        task.fail("Yandex Speller returned HTTP 500");
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getById(task.getId());

        assertThat(response.status()).isEqualTo(TaskStatus.FAILED);
        assertThat(response.text()).isNull();
        assertThat(response.error()).isEqualTo("Yandex Speller returned HTTP 500");
    }

    @Test
    void getByIdThrowsWhenTaskIsMissing() {
        UUID id = UUID.fromString("44bd78dc-d08c-41c6-b87d-fb82046bd470");
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(id))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with id: 44bd78dc-d08c-41c6-b87d-fb82046bd470 not found");
    }
}
