package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.entity.Task;
import com.taskmanager.taskmanager.enums.PriorityLevel;
import com.taskmanager.taskmanager.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findAllByDeletedAtNull(Pageable pageable);

    Page<Task> findByStatutAndDeletedAtNull(Status statut, Pageable pageable);

    Page<Task> findByAssigneId_IdAndDeletedAtNull(UUID assigneId, Pageable pageable);

    Page<Task> findByPrioritéAndDeletedAtNull(PriorityLevel priorité, Pageable pageable);

    Page<Task> findByStatutAndAssigneId_IdAndPrioritéAndDeletedAtNull(Status statut, UUID assigneId, PriorityLevel priorité, Pageable pageable);

    Page<Task> findByStatutAndAssigneId_IdAndDeletedAtNull(Status statut, UUID assigneId, Pageable pageable);

    Page<Task> findByAssigneId_IdAndPrioritéAndDeletedAtNull(UUID assigneId, PriorityLevel priorité, Pageable pageable);

    Page<Task> findByStatutAndPrioritéAndDeletedAtNull(Status statut, PriorityLevel priorité, Pageable pageable);


}
