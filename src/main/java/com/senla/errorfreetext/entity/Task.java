package com.senla.errorfreetext.entity;

import com.senla.errorfreetext.model.Language;
import com.senla.errorfreetext.model.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskStatus status;

    @Column(name = "corrected_text", columnDefinition = "TEXT")
    private String correctedText;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Task() {
    }

    public static Task create(String text, Language language) {
        Task task = new Task();
        Instant now = Instant.now();
        task.id = UUID.randomUUID();
        task.text = text;
        task.language = language;
        task.status = TaskStatus.NEW;
        task.createdAt = now;
        task.updatedAt = now;
        return task;
    }

    public void markProcessing() {
        ensureStatus(TaskStatus.NEW);
        status = TaskStatus.PROCESSING;
        updatedAt = Instant.now();
    }

    public void complete(String correctedText) {
        ensureStatus(TaskStatus.PROCESSING);
        this.correctedText = correctedText;
        this.status = TaskStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void fail(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = TaskStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    private void ensureStatus(TaskStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Task " + id + " must be in status " + expected + " but was " + status
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Language getLanguage() {
        return language;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getCorrectedText() {
        return correctedText;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
