package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.request.TaskRequest;
import com.taskmanager.taskmanager.dto.response.TaskResponse;
import com.taskmanager.taskmanager.enums.PriorityLevel;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/taches")
@RequiredArgsConstructor
@Tag(name = "Task", description = "Task API")
public class TaskController {

    private final TaskService taskService;

    @Operation(
            method = "GET",
            summary = "List all tasks",
            description = """
                    List all tasks with optional filtering by status, assignee and priority
                    Query parameters:
                    - statut (optional): Task status filter
                    - assigneId (optional): Assignee user ID filter
                    - priorité (optional): Priority level filter
                    Supports pagination parameters: page, size, sort
                    """
    )
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> listTasks(
            @RequestParam(required = false) Status statut,
            @RequestParam(required = false) UUID assigneId,
            @RequestParam(required = false) PriorityLevel priorité,
            Pageable pageable) {
        return ResponseEntity.ok(taskService.listTasks(statut, assigneId, priorité, pageable));
    }

    @Operation(
            method = "GET",
            summary = "Get task by ID",
            description = "Get a task by its unique ID"
    )
    @GetMapping("/v1/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @Operation(
            method = "POST",
            summary = "Create a task",
            description = "Create a new task. Only project managers can create tasks"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PostMapping("/v1")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(taskRequest));
    }

    @Operation(
            method = "PUT",
            summary = "Update a task",
            description = "Update an existing task by ID. Only project managers can update tasks"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PutMapping("/v1/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable UUID id, @Valid @RequestBody TaskRequest taskRequest) {
        return ResponseEntity.ok(taskService.updateTask(id, taskRequest));
    }

    @Operation(
            method = "PUT",
            summary = "Assign a task",
            description = "Assign a task to a user by task ID. Only project managers can assign tasks"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PutMapping("/v1/{id}/assigner")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable("id") UUID taskId, @RequestParam UUID assigneeId) {
        return ResponseEntity.ok(taskService.assignTask(taskId, assigneeId));
    }

    @Operation(
            method = "PUT",
            summary = "Update task status",
            description = "Update the status of a task by ID. Only project managers can update status"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PutMapping("/v1/{id}/statut")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable UUID id, @RequestParam Status status) {
        return ResponseEntity.ok(taskService.updateStatus(id, status));
    }

    @Operation(
            method = "DELETE",
            summary = "Delete a task",
            description = "Soft delete a task by ID. Only project managers can delete tasks"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            method = "GET",
            summary = "List overdue tasks",
            description = "Return all tasks that are overdue (deadline passed and not completed)"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @GetMapping("/en-retard")
    public ResponseEntity<Page<TaskResponse>> listOverdueTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.listOverdueTasks(pageable));
    }

}
