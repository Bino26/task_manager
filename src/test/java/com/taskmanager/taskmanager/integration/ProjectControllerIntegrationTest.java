package com.taskmanager.taskmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.taskmanager.dto.request.ProjectRequest;
import com.taskmanager.taskmanager.entity.Project;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private Project savedProject;

    @BeforeEach
//    void setUp() {
//        projectRepository.deleteAll();
//        userRepository.deleteAll();
//
//        userId = UUID.randomUUID();
//
//        userRepository.save(User.builder()
//                .id(userId)
//                .firstName("John")
//                .lastName("Doe")
//                .email("john.doe@example.com")
//                .build());
//
//        savedProject = projectRepository.save(Project.builder()
//                .nom("Integration Test Project")
//                .description("Integration Test Description")
//                .proprietaireId(userId)
//                .statut(Status.IN_PROGRESS)
//                .dateDebut(LocalDateTime.now())
//                .build());
//    }

    @Test
    void testFindById_ShouldReturnProject() throws Exception {
        mockMvc.perform(get("/api/projets/v1/" + savedProject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom", is("INTEGRATION TEST PROJECT")));
    }

    @Test
    void testListAllByProprietaireId_ShouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/projets/v1/list/" + userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testCreateProject_ShouldReturnCreated() throws Exception {
        ProjectRequest request = new ProjectRequest(
                userId,
                "New Integration Project",
                "Desc",
                Status.TO_DO,
                LocalDateTime.now()
        );

        mockMvc.perform(post("/api/projets/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom", is("NEW INTEGRATION PROJECT")));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testUpdateProject_ShouldReturnUpdated() throws Exception {
        ProjectRequest request = new ProjectRequest(
                userId,
                "Updated Name",
                "Updated Desc",
                Status.IN_PROGRESS,
                LocalDateTime.now()
        );

        mockMvc.perform(put("/api/projets/v1/" + savedProject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom", is("UPDATED NAME")));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testUpdateStatus_ShouldReturnUpdatedStatus() throws Exception {
        mockMvc.perform(patch("/api/projets/v1/" + savedProject.getId())
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut", is("COMPLETED")));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void testDeleteProject_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/projets/v1/" + savedProject.getId()))
                .andExpect(status().isNoContent());
    }
}