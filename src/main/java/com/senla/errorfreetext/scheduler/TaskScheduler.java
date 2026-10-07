package com.senla.errorfreetext.scheduler;

import com.senla.errorfreetext.service.TaskProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("pendingTaskScheduler")
public class TaskScheduler {

    private final TaskProcessor taskProcessor;

    public TaskScheduler(TaskProcessor taskProcessor) {
        this.taskProcessor = taskProcessor;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.fixed-delay}")
    public void processPendingTasks() {
        taskProcessor.processPendingTasks();
    }
}
