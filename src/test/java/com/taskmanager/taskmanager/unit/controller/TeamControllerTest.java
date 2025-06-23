package com.taskmanager.taskmanager.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import patika.defineX.dto.request.TeamRequest;
import patika.defineX.dto.response.TeamMemberListResponse;
import patika.defineX.dto.response.TeamResponse;
import patika.defineX.service.TeamService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TeamControllerTest {

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TeamController teamController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(teamController).build();
    }

    @Test
    void testListAllByProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        TeamResponse teamResponse = new TeamResponse(UUID.randomUUID(), projectId, "Team A");
        List<TeamResponse> teams = List.of(teamResponse);

        when(teamService.getAllTeamsByProjectId(projectId)).thenReturn(teams);

        mockMvc.perform(get("/api/teams/v1/list/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("Team A"))
                .andExpect(jsonPath("$[0].projectId").value(projectId.toString()));
    }

    @Test
    void testGetById() throws Exception {
        UUID teamId = UUID.randomUUID();
        TeamMemberListResponse teamMemberListResponse = new TeamMemberListResponse(teamId, "Team A", List.of());

        when(teamService.getTeamById(teamId)).thenReturn(teamMemberListResponse);

        mockMvc.perform(get("/api/teams/v1/{teamId}", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.name").value("Team A"));
    }

    @Test
    @WithMockUser(authorities = {"PROJECT_MANAGER"})
    void testSave() throws Exception {
        TeamRequest request = new TeamRequest("Team A", UUID.randomUUID());
        TeamResponse response = new TeamResponse(UUID.randomUUID(), request.projectId(),"Team A");

        when(teamService.save(any(TeamRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/teams/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Team A"))
                .andExpect(jsonPath("$.projectId").value(request.projectId().toString()));
    }

    @Test
    @WithMockUser(authorities = {"PROJECT_MANAGER"})
    void testAddMember() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TeamMemberListResponse response = new TeamMemberListResponse(teamId, "Team A", List.of());

        when(teamService.addTeamMember(teamId, userId)).thenReturn(response);

        mockMvc.perform(post("/api/teams/v1/members/{teamId}", teamId)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.name").value("Team A"));
    }

    @Test
    @WithMockUser(authorities = {"PROJECT_MANAGER"})
    void testRemoveMember() throws Exception {
        UUID teamMemberId = UUID.randomUUID();

        mockMvc.perform(delete("/api/teams/v1/members/{teamMemberId}", teamMemberId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"PROJECT_MANAGER"})
    void testDelete() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/teams/v1/{id}", id))
                .andExpect(status().isNoContent());
    }

}