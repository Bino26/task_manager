package com.taskmanager.taskmanager.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import patika.defineX.dto.request.ProjectRequest;
import patika.defineX.dto.response.ProjectResponse;
import patika.defineX.dto.response.TeamMemberResponse;
import patika.defineX.model.enums.ProjectStatus;
import patika.defineX.service.ProjectService;
import patika.defineX.service.TeamMemberService;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;
    @Mock
    private TeamMemberService teamMemberService;

    @InjectMocks
    private ProjectController projectController;

    private UUID projectId;
    private UUID departmentId;
    private ProjectRequest projectRequest;
    private ProjectResponse projectResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                        .build();

        projectId = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        projectRequest = new ProjectRequest(departmentId, "New Project", "Test Description", ProjectStatus.IN_PROGRESS);
        projectResponse = new ProjectResponse(projectId, "New Project", "Test Description", "IT", "ACTIVE");
    }

    @Test
    void testListAllByDepartmentId_ShouldReturnProjectList() throws Exception {

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<ProjectResponse> projectPage = new PageImpl<>(List.of(projectResponse), pageable, 1);


        when(projectService.listAllByDepartmentId(departmentId, pageable)).thenReturn(projectPage);

        mockMvc.perform(get("/api/projects/v1/list/{departmentId}", departmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(projectResponse.id().toString()));
    }

    @Test
    void getMembersByProject_ShouldReturnTeamMemberList() throws Exception {
        UUID teamMemberId = UUID.randomUUID();
        TeamMemberResponse teamMemberResponse = new TeamMemberResponse(teamMemberId, UUID.randomUUID(),UUID.randomUUID());

        when(teamMemberService.getAllMembersByProjectId(projectId)).thenReturn(List.of(teamMemberResponse));

        mockMvc.perform(get("/api/projects/all-members/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(teamMemberId.toString()));
    }

    @Test
    void testFindById_ShouldReturnProject() throws Exception {
        when(projectService.getById(projectId)).thenReturn(projectResponse);

        mockMvc.perform(get("/api/projects/v1/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Project"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testSave_WithProjectManager_ShouldCreateProject() throws Exception {
        when(projectService.save(any(ProjectRequest.class))).thenReturn(projectResponse);

        mockMvc.perform(post("/api/projects/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Project"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testUpdate_WithProjectManager_ShouldUpdateProject() throws Exception {
        when(projectService.update(eq(projectId), any(ProjectRequest.class))).thenReturn(projectResponse);

        mockMvc.perform(put("/api/projects/v1/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Project"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testUpdateStatus_WithProjectManager_ShouldUpdateProjectStatus() throws Exception {
        when(projectService.updateStatus(projectId, "COMPLETED")).thenReturn(projectResponse);

        mockMvc.perform(patch("/api/projects/v1/{id}", projectId)
                        .param("status", "COMPLETED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Project"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testDelete_WithProjectManager_ShouldDeleteProject() throws Exception {
        doNothing().when(projectService).delete(projectId);

        mockMvc.perform(delete("/api/projects/v1/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

}
