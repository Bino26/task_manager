package com.taskmanager.taskmanager.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import patika.defineX.dto.request.IssueRequest;
import patika.defineX.dto.request.IssueStatusChangeRequest;
import patika.defineX.dto.request.IssueUpdateRequest;
import patika.defineX.dto.response.IssueResponse;
import patika.defineX.model.enums.IssueStatus;
import patika.defineX.model.enums.IssueType;
import patika.defineX.model.enums.PriorityLevel;
import patika.defineX.service.IssueService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IssueControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IssueService issueService;

    @InjectMocks
    private IssueController issueController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID projectId;
    private UUID issueId;
    private UUID assigneeId;
    private UUID reporterId;
    private IssueResponse issueResponse;
    private IssueRequest issueRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(issueController).build();
        projectId = UUID.randomUUID();
        issueId = UUID.randomUUID();
        assigneeId = UUID.randomUUID();
        reporterId = UUID.randomUUID();

        issueResponse = new IssueResponse(
                issueId,
                projectId,
                assigneeId,
                reporterId,
                "Bug",
                "Test Issue",
                "Test Description",
                "User Story",
                "Acceptance Criteria",
                IssueStatus.IN_PROGRESS,
                "High",
                LocalDateTime.now()
        );

        issueRequest = new IssueRequest(
                projectId,
                assigneeId,
                "Test Issue",
                "Test Description",
                IssueType.BUG,
                PriorityLevel.HIGH,
                "User Story",
                "Acceptance Criteria",
                IssueStatus.IN_PROGRESS,
                LocalDateTime.now()
        );
    }

    @Test
    void listAllByProject_ShouldReturnIssues() throws Exception {
        when(issueService.listAllByProjectId(projectId)).thenReturn(List.of(issueResponse));

        mockMvc.perform(get("/api/issues/v1/list/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(issueId.toString()));
    }

    @Test
    void getById_ShouldReturnIssue() throws Exception {
        when(issueService.getById(issueId)).thenReturn(issueResponse);

        mockMvc.perform(get("/api/issues/v1/{id}", issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId.toString()));
    }

    @Test
    @WithMockUser(authorities = {"PROJECT_MANAGER", "TEAM_LEADER"})
    void save_ShouldCreateIssue() throws Exception {
        when(issueService.save(any(IssueRequest.class))).thenReturn(issueResponse);

        mockMvc.perform(post("/api/issues/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(issueRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(issueId.toString()));
    }

    @Test
    @WithMockUser(authorities = {"PROJECT_MANAGER", "TEAM_LEADER"})
    void update_ShouldUpdateIssue() throws Exception {
        IssueUpdateRequest updateRequest = IssueUpdateRequest.builder()
                .assigneeId(assigneeId)
                .title("Updated Title")
                .description("Updated Description")
                .type(IssueType.BUG)
                .priority(PriorityLevel.MEDIUM)
                .userStory("User Story")
                .acceptanceCriteria("Acceptance Criteria")
                .dueDate(LocalDateTime.now())
                .build();

        IssueResponse updatedResponse = new IssueResponse(
                issueId,
                projectId,
                assigneeId,
                reporterId,
                "Bug",
                "Updated Title",
                "Updated Description",
                "User Story",
                "Acceptance Criteria",
                IssueStatus.IN_PROGRESS,
                "Medium",
                LocalDateTime.now()
        );

        when(issueService.update(any(UUID.class), any(IssueUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/issues/v1/{id}", issueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateStatus_ShouldChangeIssueStatus() throws Exception {
        IssueStatusChangeRequest statusChangeRequest = new IssueStatusChangeRequest(IssueStatus.CANCELLED,"Issue resolved");

        IssueResponse updatedResponse = new IssueResponse(
                issueId,
                projectId,
                assigneeId,
                reporterId,
                "Bug",
                "Test Issue",
                "Test Description",
                "User Story",
                "Acceptance Criteria",
                IssueStatus.CANCELLED,
                "High",
                LocalDateTime.now()
        );

        when(issueService.updateStatus(any(UUID.class), any(IssueStatusChangeRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/issues/v1/status/{id}", issueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(authorities = {"PROJECT_MANAGER"})
    void delete_ShouldRemoveIssue() throws Exception {
        mockMvc.perform(delete("/api/issues/v1/{id}", issueId))
                .andExpect(status().isNoContent());
    }
}
