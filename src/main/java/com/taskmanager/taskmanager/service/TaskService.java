package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.request.TaskRequest;
import com.taskmanager.taskmanager.dto.response.TaskResponse;
import com.taskmanager.taskmanager.entity.Project;
import com.taskmanager.taskmanager.entity.Task;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.enums.PriorityLevel;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.event.TaskDeletedEvent;
import com.taskmanager.taskmanager.exception.custom.CustomNotFoundException;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Cacheable(value = "tasks")
    public Page<TaskResponse> listTasks(Status statut, UUID assigneId, PriorityLevel priorité, Pageable pageable) {
        log.info("Fetching tasks with filters - statut: {}, assigneId: {}, priorité: {}", statut, assigneId, priorité);

        // Filtering logic — you might want to implement a Specification or QueryDSL for flexible queries.
        // For simplicity, here is an example with all-null returning all tasks not deleted.
        if (statut != null && assigneId != null && priorité != null) {
            return taskRepository.findByStatutAndAssigneId_IdAndPrioritéAndDeletedAtNull(statut, assigneId, priorité, pageable)
                    .map(TaskResponse::from);
        }
        if (statut != null && assigneId != null) {
            return taskRepository.findByStatutAndAssigneId_IdAndDeletedAtNull(statut, assigneId, pageable)
                    .map(TaskResponse::from);
        }
        if (assigneId != null && priorité != null) {
            return taskRepository.findByAssigneId_IdAndPrioritéAndDeletedAtNull(assigneId, priorité, pageable)
                    .map(TaskResponse::from);
        }
        if (statut != null && priorité != null) {
            return taskRepository.findByStatutAndPrioritéAndDeletedAtNull(statut, priorité, pageable)
                    .map(TaskResponse::from);
        }
        if (statut != null) {
            return taskRepository.findByStatutAndDeletedAtNull(statut, pageable)
                    .map(TaskResponse::from);
        }
        if (assigneId != null) {
            return taskRepository.findByAssigneId_IdAndDeletedAtNull(assigneId, pageable)
                    .map(TaskResponse::from);
        }
        if (priorité != null) {
            return taskRepository.findByPrioritéAndDeletedAtNull(priorité, pageable)
                    .map(TaskResponse::from);
        }

        log.info("No filters applied, fetching all tasks");
        return taskRepository.findAllByDeletedAtNull(pageable).map(TaskResponse::from);
    }

    @Cacheable(value = "tasks", key = "#id")
    public TaskResponse getById(UUID id) {
        log.info("Fetching task by id: {}", id);
        Task task = findById(id);
        return TaskResponse.from(task);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating task with title: {}", request.titre());

        Project project = projectRepository.findById(request.projetId())
                .orElseThrow(() -> new CustomNotFoundException("Project not found with id: " + request.projetId()));

        User assignee = userRepository.findById(request.assigneId())
                .orElseThrow(() -> new CustomNotFoundException("User not found with id: " + request.assigneId()));

        Task task = Task.builder()
                .titre(request.titre())
                .description(request.description())
                .dateEcheance(request.dateEcheance())
                .priorité(request.priorité())
                .projetId(project)
                .assigneId(assignee)
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("Task created with id: {}", savedTask.getId());

        return TaskResponse.from(savedTask);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskResponse updateTask(UUID id, TaskRequest request) {
        log.info("Updating task with id: {}", id);
        Task task = findById(id);

        if (request.titre() != null) task.setTitre(request.titre());
        if (request.description() != null) task.setDescription(request.description());
        if (request.dateEcheance() != null) task.setDateEcheance(request.dateEcheance());
        if (request.priorité() != null) task.setPriorité(request.priorité());

        if (request.projetId() != null) {
            Project project = projectRepository.findById(request.projetId())
                    .orElseThrow(() -> new CustomNotFoundException("Project not found with id: " + request.projetId()));
            task.setProjetId(project);
        }

        if (request.assigneId() != null) {
            User assignee = userRepository.findById(request.assigneId())
                    .orElseThrow(() -> new CustomNotFoundException("User not found with id: " + request.assigneId()));
            task.setAssigneId(assignee);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task with id: {} updated successfully", id);
        return TaskResponse.from(updatedTask);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskResponse assignTask(UUID taskId, UUID assigneeId) {
        log.info("Assigning task {} to user {}", taskId, assigneeId);
        Task task = findById(taskId);

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new CustomNotFoundException("User not found with id: " + assigneeId));

        task.setAssigneId(assignee);
        Task savedTask = taskRepository.save(task);
        log.info("Task {} assigned to user {}", taskId, assigneeId);

        return TaskResponse.from(savedTask);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskResponse updateStatus(UUID taskId, Status status) {
        log.info("Updating status of task {} to {}", taskId, status);
        Task task = findById(taskId);

        task.setStatut(status);
        Task savedTask = taskRepository.save(task);

        log.info("Task {} status updated to {}", taskId, status);
        return TaskResponse.from(savedTask);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public void deleteTask(UUID taskId) {
        log.warn("Soft deleting task with id: {}", taskId);
        Task task = findById(taskId);
        task.setDeletedAt(LocalDateTime.from(Instant.now()));
        taskRepository.save(task);
        applicationEventPublisher.publishEvent(new TaskDeletedEvent(task.getId()));
        log.warn("Task with id: {} marked as deleted", taskId);
    }

    protected Task findById(UUID id) {
        log.debug("Searching for task with id: {}", id);
        return taskRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> {
                    log.error("Task with id: {} not found or deleted", id);
                    return new CustomNotFoundException("Task not found with id: " + id);
                });
    }

    @Cacheable(value = "tasks", key = "'overdue'")
    public Page<TaskResponse> listOverdueTasks(Pageable pageable) {
        log.info("Fetching overdue tasks");
        return taskRepository.findOverdueTasks(pageable)
                .map(TaskResponse::from);
    }

}
