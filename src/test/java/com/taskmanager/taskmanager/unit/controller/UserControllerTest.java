package com.taskmanager.taskmanager.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.taskmanager.controller.UserController;
import com.taskmanager.taskmanager.dto.request.UserRequest;
import com.taskmanager.taskmanager.dto.response.UserResponse;
import com.taskmanager.taskmanager.enums.Role;
import com.taskmanager.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserResponse userResponse;
    private UserResponse updatedUserResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        UUID userId = UUID.randomUUID();
        Role role = Role.PROJECT_MANAGER;
        userResponse = new UserResponse(userId, "User One", "user1@example.com", List.of(role.getDisplayName()));
        updatedUserResponse = new UserResponse(userId, "Updated User", "updated@example.com", List.of(role.getDisplayName()));
    }

    @Test
    void testListUsers() throws Exception {
        when(userService.listAll()).thenReturn(Arrays.asList(userResponse, updatedUserResponse));

        mockMvc.perform(get("/api/utilisateurs/v1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("User One"))
                .andExpect(jsonPath("$[1].name").value("Updated User"));

        verify(userService, times(1)).listAll();
    }

    @Test
    void testGetUserById() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.getById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/utilisateurs/v1/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("User One"))
                .andExpect(jsonPath("$.email").value("user1@example.com"));

        verify(userService, times(1)).getById(userId);
    }

    @Test
    void testUpdateUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRequest userRequest = new UserRequest("Updated User", "updated@example.com", "newpassword123");

        when(userService.update(userId, userRequest)).thenReturn(updatedUserResponse);

        mockMvc.perform(put("/api/utilisateurs/v1/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).update(userId, userRequest);
    }

    @Test
    void testAddRole() throws Exception {
        UUID userId = UUID.randomUUID();
        Role role = Role.TEAM_MEMBER;
        UserResponse userResponse = new UserResponse(userId, "User One", "user1@example.com", List.of(role.getDisplayName()));

        when(userService.addRole(userId, role)).thenReturn(userResponse);

        mockMvc.perform(patch("/api/utilisateurs/v1/{id}/roles/add", userId)
                        .param("role", role.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("User One"))
                .andExpect(jsonPath("$.authorities[0]").value(role.getDisplayName()));

        verify(userService, times(1)).addRole(userId, role);
    }

    @Test
    void testRemoveRole() throws Exception {
        UUID userId = UUID.randomUUID();
        Role role = Role.TEAM_MEMBER;

        UserResponse userResponse = new UserResponse(userId, "User One", "user1@example.com", List.of(role.getDisplayName()));

        when(userService.removeRole(userId, role)).thenReturn(userResponse);

        mockMvc.perform(patch("/api/utilisateurs/v1/{id}/roles/remove", userId)
                        .param("role", role.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("User One"))
                .andExpect(jsonPath("$.authorities").value(role.getDisplayName()));

        verify(userService, times(1)).removeRole(userId, role);
    }

    @Test
    void testDeleteUser() throws Exception {
        UUID userId = UUID.randomUUID();

        doNothing().when(userService).delete(userId);

        mockMvc.perform(delete("/api/utilisateurs/v1/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(userId);
    }
}
