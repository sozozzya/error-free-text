package com.senla.errorfreetext.service;

import com.senla.errorfreetext.controller.dto.CreateTaskRequest;
import com.senla.errorfreetext.controller.dto.CreateTaskResponse;
import com.senla.errorfreetext.controller.dto.TaskResponse;
import com.senla.errorfreetext.entity.Task;
import com.senla.errorfreetext.exception.TaskNotFoundException;
import com.senla.errorfreetext.repository.TaskRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public CreateTaskResponse create(CreateTaskRequest request) {
        Task task = Task.create(request.text(), request.language());
        taskRepository.save(task);
        log.info("Created correction task {}", task.getId());
        return new CreateTaskResponse(task.getId());
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return TaskResponse.from(task);
    }
}
