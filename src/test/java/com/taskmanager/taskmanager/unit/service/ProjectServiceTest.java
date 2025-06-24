package com.taskmanager.taskmanager.unit.service;

import com.taskmanager.taskmanager.dto.request.ProjectRequest;
import com.taskmanager.taskmanager.dto.response.ProjectResponse;
import com.taskmanager.taskmanager.entity.Project;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.event.ProjectDeletedEvent;
import com.taskmanager.taskmanager.event.ProprietaireDeletedEvent;
import com.taskmanager.taskmanager.exception.custom.CustomAlreadyExistException;
import com.taskmanager.taskmanager.exception.custom.CustomNotFoundException;
import com.taskmanager.taskmanager.exception.custom.StatusChangeException;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.service.ProjectService;
import com.taskmanager.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private UUID projectId;
    private UUID userId;
    private Project project;
    private ProjectRequest projectRequest;
    private User proprietaire;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        projectRequest = new ProjectRequest(
                userId,
                "Test Project",
                "desc",
                Status.IN_PROGRESS,
                LocalDateTime.now()  // or any other valid LocalDateTime
        );

        proprietaire = User.builder()
                .nomUtilisateur("Owner Name")
                .email("owner@example.com")
                .build();
        proprietaire.setId(userId);

        project = Project.builder()
                .nom("TEST PROJECT")
                .description("desc")
                .statut(Status.IN_PROGRESS)
                .proprietaireId(proprietaire)
                .build();
        project.setId(projectId);
    }

    @Test
    void save_ShouldSaveProject() {
        when(projectRepository.existsByNomAndDeletedAtNull("TEST PROJECT")).thenReturn(false);
        when(userService.findById(userId)).thenReturn(proprietaire);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.save(projectRequest);

        assertNotNull(response);
        assertEquals("TEST PROJECT", response.getNom());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void save_WhenDuplicateTitle_ShouldThrowException() {
        when(projectRepository.existsByNomAndDeletedAtNull("TEST PROJECT")).thenReturn(true);

        CustomAlreadyExistException exception = assertThrows(CustomAlreadyExistException.class,
                () -> projectService.save(projectRequest));
        assertEquals("Project already exists with name: TEST PROJECT", exception.getMessage());
    }

    @Test
    void getById_ShouldReturnProjectResponse() {
        when(projectRepository.findByIdAndDeletedAtNull(projectId)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getById(projectId);

        assertNotNull(response);
        assertEquals(projectId, response.getId());
    }

    @Test
    void getById_WhenNotFound_ShouldThrow() {
        when(projectRepository.findByIdAndDeletedAtNull(projectId)).thenReturn(Optional.empty());

        CustomNotFoundException ex = assertThrows(CustomNotFoundException.class, () -> projectService.getById(projectId));
        assertEquals("Project not found with id: " + projectId, ex.getMessage());
    }

    @Test
    void listAllByProprietaireId_ShouldReturnProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(project), pageable, 1);

        when(userService.findById(userId)).thenReturn(proprietaire);
        when(projectRepository.findAllWithPaginationByProprietaireId_IdAndDeletedAtNull(userId, pageable))
                .thenReturn(page);

        Page<ProjectResponse> result = projectService.listAllByProprietaireId(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("TEST PROJECT", result.getContent().get(0).getNom());
    }

    @Test
    void update_ShouldUpdateProject() {
        when(projectRepository.findByIdAndDeletedAtNull(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.existsByNomAndDeletedAtNull("TEST PROJECT")).thenReturn(false);
        when(userService.findById(userId)).thenReturn(proprietaire);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.update(projectId, projectRequest);

        assertNotNull(response);
        assertEquals("TEST PROJECT", response.getNom());
    }

    @Test
    void update_WhenProjectNotFound_ShouldThrow() {
        when(projectRepository.findByIdAndDeletedAtNull(projectId)).thenReturn(Optional.empty());

        CustomNotFoundException ex = assertThrows(CustomNotFoundException.class,
                () -> projectService.update(projectId, projectRequest));
        assertEquals("Project not found with id: " + projectId, ex.getMessage());
    }

    @Test
    void updateStatus_ShouldUpdateProjectStatus() {
        when(projectRepository.findByIdAndDeletedAtNull(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.updateStatus(projectId, "COMPLETED");

        assertEquals(Status.COMPLETED.getDisplayName(), response.getStatus());
    }

    @Test
    void updateStatus_WhenAlreadyCompleted_ShouldThrow() {
        project.setStatut(Status.COMPLETED);
        when(projectRepository.findByIdAndDeletedAtNull(projectId)).thenReturn(Optional.of(project));

        StatusChangeException ex = assertThrows(StatusChangeException.class,
                () -> projectService.updateStatus(projectId, "ACTIVE"));
        assertEquals("Project status cannot be changed because it is completed.", ex.getMessage());
    }

    @Test
    void delete_ShouldSoftDeleteAndPublishEvent() {
        when(projectRepository.findByIdAndDeletedAtNull(projectId)).thenReturn(Optional.of(project));

        projectService.delete(projectId);

        verify(projectRepository).save(project);
        verify(applicationEventPublisher).publishEvent(any(ProjectDeletedEvent.class));
    }

    @Test
    void delete_WhenProjectNotFound_ShouldThrow() {
        when(projectRepository.findByIdAndDeletedAtNull(projectId)).thenReturn(Optional.empty());

        CustomNotFoundException ex = assertThrows(CustomNotFoundException.class,
                () -> projectService.delete(projectId));
        assertEquals("Project not found with id: " + projectId, ex.getMessage());
    }

    @Test
    void deleteAllByProprietaireId_ShouldSoftDeleteAllProjects() {
        ProprietaireDeletedEvent event = new ProprietaireDeletedEvent(userId);
        when(projectRepository.findAllByProprietaireId_IdAndDeletedAtNull(userId)).thenReturn(List.of(project));

        projectService.deleteAllByProprietaireId(event);

        verify(projectRepository).saveAll(anyList());
        verify(applicationEventPublisher).publishEvent(any(ProjectDeletedEvent.class));
    }
}
