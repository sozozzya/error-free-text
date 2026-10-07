package com.senla.errorfreetext.service;

import com.senla.errorfreetext.entity.Task;
import com.senla.errorfreetext.exception.SpellerClientException;
import com.senla.errorfreetext.exception.TaskNotFoundException;
import com.senla.errorfreetext.model.TaskStatus;
import com.senla.errorfreetext.repository.TaskRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TaskProcessor {

    private static final Logger log = LoggerFactory.getLogger(TaskProcessor.class);

    private final TaskRepository taskRepository;
    private final TextCorrectionService textCorrectionService;
    private final TransactionTemplate transactionTemplate;

    public TaskProcessor(
            TaskRepository taskRepository,
            TextCorrectionService textCorrectionService,
            TransactionTemplate transactionTemplate
    ) {
        this.taskRepository = taskRepository;
        this.textCorrectionService = textCorrectionService;
        this.transactionTemplate = transactionTemplate;
    }

    public void processPendingTasks() {
        List<Task> pendingTasks = taskRepository.findByStatusOrderByCreatedAtAsc(TaskStatus.NEW);
        for (Task task : pendingTasks) {
            processTask(task.getId());
        }
    }

    public void processTask(UUID taskId) {
        try {
            Task task = markProcessing(taskId);
            log.info("Started processing task {}", taskId);
            String correctedText = textCorrectionService.correct(task.getText(), task.getLanguage());
            complete(taskId, correctedText);
            log.info("Completed processing task {}", taskId);
        } catch (Exception ex) {
            log.error("Failed to process task {}: {}", taskId, ex.getMessage());
            try {
                fail(taskId, toErrorMessage(ex));
            } catch (Exception failEx) {
                log.error("Unable to mark task {} as FAILED: {}", taskId, failEx.getMessage());
            }
        }
    }

    private Task markProcessing(UUID taskId) {
        return transactionTemplate.execute(status -> {
            Task task = getTask(taskId);
            task.markProcessing();
            return taskRepository.save(task);
        });
    }

    private void complete(UUID taskId, String correctedText) {
        transactionTemplate.executeWithoutResult(status -> {
            Task task = getTask(taskId);
            task.complete(correctedText);
            taskRepository.save(task);
        });
    }

    private void fail(UUID taskId, String errorMessage) {
        transactionTemplate.executeWithoutResult(status -> {
            Task task = getTask(taskId);
            task.fail(errorMessage);
            taskRepository.save(task);
        });
    }

    private Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private String toErrorMessage(Exception ex) {
        if (ex instanceof SpellerClientException && ex.getMessage() != null) {
            return ex.getMessage();
        }
        return "Unexpected error during text correction";
    }
}
