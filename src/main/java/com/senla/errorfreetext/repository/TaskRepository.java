package com.senla.errorfreetext.repository;

import com.senla.errorfreetext.entity.Task;
import com.senla.errorfreetext.model.TaskStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByStatusOrderByCreatedAtAsc(TaskStatus status);
}
