package com.taskmanager.taskmanager.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.taskmanager.controller.TaskController;
import com.taskmanager.taskmanager.dto.request.TaskRequest;
import com.taskmanager.taskmanager.dto.response.TaskResponse;
import com.taskmanager.taskmanager.enums.PriorityLevel;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskRequest taskRequest;
    private TaskResponse taskResponse;
    private UUID taskId;

    @BeforeEach
    void setup() {
        taskId = UUID.randomUUID();
        taskRequest = new TaskRequest(
                "Titre",
                "Description",
                Instant.now().plusSeconds(3600),
                PriorityLevel.MEDIUM,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        taskResponse = new TaskResponse(
                        taskId,
                        taskRequest.titre(),
                        taskRequest.description(),
                        taskRequest.dateEcheance(),
                        5L,
                Status.TO_DO.getDisplayName(),
                PriorityLevel.MEDIUM.getDisplayName(),
                        taskRequest.projetId(),
                        taskRequest.assigneId()
                );
    }

    @Test
    void shouldListTasks() throws Exception {
        Mockito.when(taskService.listTasks(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(taskResponse)));

        mockMvc.perform(get("/api/taches")
                        .param("statut", "TODO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetTaskById() throws Exception {
        Mockito.when(taskService.getById(taskId)).thenReturn(taskResponse);

        mockMvc.perform(get("/api/taches/v1/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldCreateTask() throws Exception {
        Mockito.when(taskService.createTask(any())).thenReturn(taskResponse);

        mockMvc.perform(post("/api/taches/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titre").value("Titre"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldUpdateTask() throws Exception {
        Mockito.when(taskService.updateTask(eq(taskId), any())).thenReturn(taskResponse);

        mockMvc.perform(put("/api/taches/v1/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldAssignTask() throws Exception {
        UUID assigneeId = UUID.randomUUID();
        Mockito.when(taskService.assignTask(taskId, assigneeId)).thenReturn(taskResponse);

        mockMvc.perform(put("/api/taches/v1/{id}/assigner", taskId)
                        .param("assigneeId", assigneeId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldUpdateTaskStatus() throws Exception {
        Mockito.when(taskService.updateStatus(taskId, Status.IN_PROGRESS)).thenReturn(taskResponse);

        mockMvc.perform(put("/api/taches/v1/{id}/statut", taskId)
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TODO")); // assuming it's not updated in the mock
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/taches/v1/{id}", taskId))
                .andExpect(status().isNoContent());

        Mockito.verify(taskService).deleteTask(taskId);
    }

    @Test
    @WithMockUser(authorities = "PROJECT_MANAGER")
    void shouldListOverdueTasks() throws Exception {
        Mockito.when(taskService.listOverdueTasks(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(taskResponse)));

        mockMvc.perform(get("/api/taches/en-retard"))
                .andExpect(status().isOk());
    }
}