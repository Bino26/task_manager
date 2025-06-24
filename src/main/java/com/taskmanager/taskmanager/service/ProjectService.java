package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.request.ProjectRequest;
import com.taskmanager.taskmanager.dto.response.ProjectResponse;
import com.taskmanager.taskmanager.entity.Project;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.event.ProjectDeletedEvent;
import com.taskmanager.taskmanager.event.ProprietaireDeletedEvent;
import com.taskmanager.taskmanager.exception.custom.CustomAlreadyExistException;
import com.taskmanager.taskmanager.exception.custom.CustomNotFoundException;
import com.taskmanager.taskmanager.exception.custom.StatusChangeException;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;



    @Cacheable(value = "projects")
    public Page<ProjectResponse> listAllByProprietaireId(UUID proprietaireId, Pageable pageable) {
        log.info("Fetching projects for proprietaire with id: {} from database", proprietaireId);
        userService.findById(proprietaireId);
        Page<ProjectResponse> projects = projectRepository.findAllWithPaginationByProprietaireId_IdAndDeletedAtNull(proprietaireId, pageable)
                .map(ProjectResponse::from);
        log.info("{} projects found for proprietaire id: {}", projects.getTotalElements(), proprietaireId);
        return projects;
    }

    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse getById(UUID id) {
        log.info("Fetching project with id: {} from database", id);
        ProjectResponse response = ProjectResponse.from(findById(id));
        log.info("Project with id: {} retrieved successfully.", id);
        return response;
    }

    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse save(ProjectRequest projectRequest) {
        log.info("Creating a new project with title: {}", projectRequest.nom());
        existsByTitle(projectRequest.nom().toUpperCase());
        Project project = ProjectRequest.from(projectRequest);
        project.setProprietaireId(userService.findById(projectRequest.proprietaireId()));
        ProjectResponse response = ProjectResponse.from(projectRepository.save(project));
        log.info("Project '{}' created successfully with id: {}", project.getNom(), project.getId());
        return response;
    }

    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse update(UUID id, ProjectRequest projectRequest) {
        log.info("Updating project with id: {}", id);
        Project project = findById(id);

        if (!project.getNom().equalsIgnoreCase(projectRequest.nom())) {
            existsByTitle(projectRequest.nom().toUpperCase());
        }

        project.setNom(projectRequest.nom().toUpperCase());
        project.setDescription(projectRequest.description());
        project.setStatut(projectRequest.statut());
        project.setProprietaireId(userService.findById(projectRequest.proprietaireId()));

        ProjectResponse response = ProjectResponse.from(projectRepository.save(project));
        log.info("Project with id: {} updated successfully. New title: {}", id, project.getNom());
        return response;
    }

    @CacheEvict(value = "projects", allEntries = true)
    @Transactional
    public void delete(UUID id) {
        log.warn("Attempting to delete project with id: {}", id);
        Project project = findById(id);
        project.softDelete();
        projectRepository.save(project);
        applicationEventPublisher.publishEvent(new ProjectDeletedEvent(project.getId()));
        log.warn("Project with id: {} has been marked as deleted.", id);
    }

    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse updateStatus(UUID id, String status) {
        log.info("Updating project status for id: {} to {}", id, status);
        Project project = findById(id);

        if (project.getStatut().equals(Status.COMPLETED)) {
            log.error("Project status update failed for id: {}. Project is already completed.", id);
            throw new StatusChangeException("Project status cannot be changed because it is completed.");
        }

        project.setStatut(Status.valueOf(status));
        ProjectResponse response = ProjectResponse.from(projectRepository.save(project));
        log.info("Project with id: {} status updated successfully to {}", id, project.getStatut());
        return response;
    }

    @CacheEvict(value = "projects", allEntries = true)
    @EventListener
    @Transactional
    public void deleteAllByProprietaireId(ProprietaireDeletedEvent event) {
        log.warn("Deleting all projects for proprietaire id: {}", event.proprietaireId());

        List<Project> projects = projectRepository.findAllByProprietaireId_IdAndDeletedAtNull(event.proprietaireId());

        projects.forEach(project -> {
            project.softDelete();
            applicationEventPublisher.publishEvent(new ProjectDeletedEvent(project.getId()));
        });

        projectRepository.saveAll(projects);

        log.warn("{} projects deleted for proprietaire id: {}", projects.size(), event.proprietaireId());
    }

    protected Project findById(UUID id) {
        log.debug("Searching for project with id: {}", id);
        return projectRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> {
                    log.error("Project with id: {} not found!", id);
                    return new CustomNotFoundException("Project not found with id: " + id);
                });
    }

    private void existsByTitle(String name) {
        log.debug("Checking if project with title '{}' already exists...", name);
        if (projectRepository.existsByNomAndDeletedAtNull(name)) {
            log.error("Project with title '{}' already exists!", name);
            throw new CustomAlreadyExistException("Project already exists with name: " + name);
        }
    }
}
