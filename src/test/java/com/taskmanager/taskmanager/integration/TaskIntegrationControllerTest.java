package com.taskmanager.taskmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.taskmanager.dto.request.TaskRequest;
import com.taskmanager.taskmanager.enums.PriorityLevel;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.entity.Project;
import com.taskmanager.taskmanager.entity.Task;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TaskRepository taskRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;

    private UUID projectId;
    private UUID userId;
    private UUID createdTaskId;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();

        Project project = Project.builder()
                .nom("Integration Project")
                .description("For integration test")
                .statut(Status.TO_DO)
                .dateDebut(LocalDateTime.from(Instant.now()))
                .build();
        project = projectRepository.save(project);
        projectId = project.getId();

        User user = User.builder()
                .nomUtilisateur("Jane")
                .email("jane.doe@example.com")
                .build();
        user = userRepository.save(user);
        userId = user.getId();
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldCreateAndFetchTaskSuccessfully() throws Exception {
        TaskRequest request = new TaskRequest(
                "Integration Task",
                "Integration description",
                Instant.now().plusSeconds(86400),
                PriorityLevel.MEDIUM,
                projectId,
                userId
        );

        String response = mockMvc.perform(post("/api/taches/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titre", is("Integration Task")))
                .andReturn().getResponse().getContentAsString();

        createdTaskId = objectMapper.readTree(response).get("id").traverse().readValueAs(UUID.class);

        mockMvc.perform(get("/api/taches/v1/" + createdTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", is("Integration Task")));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldUpdateTaskSuccessfully() throws Exception {
        Task task = createTask("Initial Task");

        TaskRequest updateRequest = new TaskRequest(
                "Updated Task",
                "Updated description",
                Instant.now().plusSeconds(86400),
                PriorityLevel.HIGH,
                projectId,
                userId
        );

        mockMvc.perform(put("/api/taches/v1/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", is("Updated Task")));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldAssignTaskSuccessfully() throws Exception {
        Task task = createTask("Task to assign");

        mockMvc.perform(put("/api/taches/v1/" + task.getId() + "/assigner")
                        .param("assigneeId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneId", is(userId.toString())));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldUpdateStatusSuccessfully() throws Exception {
        Task task = createTask("Task to update status");

        mockMvc.perform(put("/api/taches/v1/" + task.getId() + "/statut")
                        .param("status", "EN_COURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut", is("EN_COURS")));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldDeleteTaskSuccessfully() throws Exception {
        Task task = createTask("Task to delete");

        mockMvc.perform(delete("/api/taches/v1/" + task.getId()))
                .andExpect(status().isNoContent());
    }

    private Task createTask(String title) {
        Task task = Task.builder()
                .titre(title)
                .description("Desc")
                .dateEcheance(Instant.now().plusSeconds(3600))
                .priorité(PriorityLevel.MEDIUM)
                .projetId(projectRepository.findById(projectId).get())
                .assigneId(userRepository.findById(userId).get())
                .build();
        return taskRepository.save(task);
    }
}
