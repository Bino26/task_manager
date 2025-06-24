package com.taskmanager.taskmanager.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.taskmanager.controller.ProjectController;
import com.taskmanager.taskmanager.dto.request.ProjectRequest;
import com.taskmanager.taskmanager.dto.response.ProjectResponse;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private UUID projectId;
    private UUID proprietaireId;
    private ProjectRequest projectRequest;
    private ProjectResponse projectResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        projectId = UUID.randomUUID();
        proprietaireId = UUID.randomUUID();

        projectRequest = new ProjectRequest(
                proprietaireId,
                "New Project",
                "Test Description",
                Status.IN_PROGRESS,
                LocalDateTime.now()
        );

        projectResponse = new ProjectResponse(
                projectId,
                "New Project",
                "Test Description",
                "IT",
                "ACTIVE"
        );
    }

    @Test
    void testListAllByProprietaireId_ShouldReturnProjectList() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<ProjectResponse> projectPage = new PageImpl<>(List.of(projectResponse), pageable, 1);

        when(projectService.listAllByProprietaireId(proprietaireId, pageable)).thenReturn(projectPage);

        mockMvc.perform(get("/api/projets/v1/list/{proprietaireId}", proprietaireId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(projectResponse.getId().toString()));
    }

    @Test
    void testFindById_ShouldReturnProject() throws Exception {
        when(projectService.getById(projectId)).thenReturn(projectResponse);

        mockMvc.perform(get("/api/projets/v1/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("New Project"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testSave_WithProjectManager_ShouldCreateProject() throws Exception {
        when(projectService.save(any(ProjectRequest.class))).thenReturn(projectResponse);

        mockMvc.perform(post("/api/projets/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("New Project"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testUpdate_WithProjectManager_ShouldUpdateProject() throws Exception {
        when(projectService.update(eq(projectId), any(ProjectRequest.class))).thenReturn(projectResponse);

        mockMvc.perform(put("/api/projets/v1/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("New Project"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testUpdateStatus_WithProjectManager_ShouldUpdateProjectStatus() throws Exception {
        when(projectService.updateStatus(projectId, "COMPLETED")).thenReturn(projectResponse);

        mockMvc.perform(patch("/api/projets/v1/{id}", projectId)
                        .param("status", "COMPLETED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("New Project"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testDelete_WithProjectManager_ShouldDeleteProject() throws Exception {
        doNothing().when(projectService).delete(projectId);

        mockMvc.perform(delete("/api/projets/v1/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
