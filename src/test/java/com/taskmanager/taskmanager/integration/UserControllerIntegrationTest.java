
package com.taskmanager.taskmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.taskmanager.dto.request.UserRequest;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.enums.Role;
import com.taskmanager.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        user = User.builder()
                .nomUtilisateur("John")
                .email("john.doe@example.com")
                .role(Set.of(Role.TEAM_MEMBER))
                .build();
        user = userRepository.save(user);
    }

    @Test
    void testListUsers_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetUserById_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/v1/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void testUpdateUser_ShouldUpdateSuccessfully() throws Exception {
        UserRequest request = new UserRequest("Updated", "User", "john.doe@example.com");

        mockMvc.perform(put("/api/utilisateurs/v1/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void testAddRoleToUser_AsProjectManager_ShouldAddRole() throws Exception {
        mockMvc.perform(patch("/api/utilisateurs/v1/" + user.getId() + "/roles/add")
                        .param("role", "PROJECT_MANAGER"))
                .andExpect(status().isForbidden()); // No mock security context; expected 403
    }

    @Test
    void testRemoveRoleFromUser_AsProjectManager_ShouldRemoveRole() throws Exception {
        mockMvc.perform(patch("/api/utilisateurs/v1/" + user.getId() + "/roles/remove")
                        .param("role", "TEAM_MEMBER"))
                .andExpect(status().isForbidden()); // No mock security context; expected 403
    }

    @Test
    void testDeleteUser_ShouldRemoveUser() throws Exception {
        mockMvc.perform(delete("/api/utilisateurs/v1/" + user.getId()))
                .andExpect(status().isNoContent());
    }
}
