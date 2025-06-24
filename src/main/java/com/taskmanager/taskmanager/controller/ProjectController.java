package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.request.ProjectRequest;
import com.taskmanager.taskmanager.dto.response.ProjectResponse;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.service.ProjectService;
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


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project API")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            method = "GET",
            summary = "List all projects",
            description = """
                    List all projects in the system with pagination
                    You can customize the results using query parameters:
                    - `page`: The page number (default: 0).
                    - `size`: The number of records per page (default: 20).
                    - `sort`: Sorting criteria in the format  Examples: `createdAt,asc` (default)
                    """
    )
    @GetMapping("/v1/list/{proprietaireId}")
    public ResponseEntity<Page<ProjectResponse>> listAllByProprietaireId(@PathVariable UUID proprietaireId, Pageable pageable) {
        return ResponseEntity.ok(projectService.listAllByProprietaireId(proprietaireId, pageable));
    }

    @Operation(
            method = "GET",
            summary = "Get project",
            description = "Get project by id"
    )
    @GetMapping("/v1/{id}")
    public ResponseEntity<ProjectResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @Operation(
            method = "POST",
            summary = "Create a project",
            description = "Create a project. Only project managers can create a project"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PostMapping("/v1")
    public ResponseEntity<ProjectResponse> save(@Valid @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.save(projectRequest));
    }

    @Operation(
            method = "PUT",
            summary = "Update a project",
            description = "Update a project by id with new values. Only project managers can update a project"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable UUID id,@Valid @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.ok(projectService.update(id, projectRequest));
    }

    @Operation(
            method = "PATCH",
            summary = "Update project status",
            description = "Update project status by id. Only project managers can update project status"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PatchMapping("/v1/{id}")
    public ResponseEntity<ProjectResponse> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        return ResponseEntity.ok(projectService.updateStatus(id, status));
    }

    @Operation(
            method = "DELETE",
            summary = "Delete a project",
            description = "Delete a project by id. Only project managers can delete a project"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            method = "GET",
            summary = "List  projects per statut",
            description = "List  projects per statut"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> listProjects(
            @RequestParam(required = false) Status statut,
            Pageable pageable
    ) {
        return ResponseEntity.ok(projectService.listProjects(statut, pageable));
    }
}
