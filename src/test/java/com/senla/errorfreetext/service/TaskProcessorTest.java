package com.senla.errorfreetext.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.senla.errorfreetext.entity.Task;
import com.senla.errorfreetext.exception.SpellerClientException;
import com.senla.errorfreetext.model.Language;
import com.senla.errorfreetext.model.TaskStatus;
import com.senla.errorfreetext.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class TaskProcessorTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TextCorrectionService textCorrectionService;

    private TaskProcessor taskProcessor;

    @BeforeEach
    void setUp() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(new NoOpTransactionManager());
        taskProcessor = new TaskProcessor(taskRepository, textCorrectionService, transactionTemplate);
    }

    @Test
    void processesNewTaskUntilCompleted() {
        Task task = Task.create("teh text", Language.EN);
        when(taskRepository.findByStatusOrderByCreatedAtAsc(TaskStatus.NEW)).thenReturn(List.of(task));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(textCorrectionService.correct("teh text", Language.EN)).thenReturn("the text");

        taskProcessor.processPendingTasks();

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getCorrectedText()).isEqualTo("the text");
        verify(textCorrectionService).correct("teh text", Language.EN);
    }

    @Test
    void marksTaskFailedWhenSpellerFailsAndKeepsErrorMessage() {
        Task task = Task.create("teh text", Language.EN);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(textCorrectionService.correct("teh text", Language.EN))
                .thenThrow(new SpellerClientException("Yandex Speller returned HTTP 503"));

        taskProcessor.processTask(task.getId());

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).isEqualTo("Yandex Speller returned HTTP 503");
        assertThat(task.getCorrectedText()).isNull();
    }

    @Test
    void continuesProcessingRemainingTasksAfterOneFailure() {
        Task failed = Task.create("bad text", Language.EN);
        Task successful = Task.create("teh text", Language.EN);
        when(taskRepository.findByStatusOrderByCreatedAtAsc(TaskStatus.NEW)).thenReturn(List.of(failed, successful));
        when(taskRepository.findById(failed.getId())).thenReturn(Optional.of(failed));
        when(taskRepository.findById(successful.getId())).thenReturn(Optional.of(successful));
        when(taskRepository.save(failed)).thenReturn(failed);
        when(taskRepository.save(successful)).thenReturn(successful);
        when(textCorrectionService.correct("bad text", Language.EN))
                .thenThrow(new SpellerClientException("timeout"));
        when(textCorrectionService.correct("teh text", Language.EN)).thenReturn("the text");

        taskProcessor.processPendingTasks();

        assertThat(failed.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(successful.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(successful.getCorrectedText()).isEqualTo("the text");
    }

    private static final class NoOpTransactionManager implements PlatformTransactionManager {

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
        }

        @Override
        public void rollback(TransactionStatus status) {
        }
    }
}
