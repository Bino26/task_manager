package com.taskmanager.taskmanager.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import patika.defineX.dto.request.IssueCommentRequest;
import patika.defineX.dto.response.IssueCommentResponse;
import patika.defineX.service.IssueCommentService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class IssueCommentControllerTest {

    @Mock
    private IssueCommentService issueCommentService;

    @InjectMocks
    private IssueCommentController issueCommentController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(issueCommentController).build();
    }

    @Test
    void testList() throws Exception {
        UUID issueId = UUID.randomUUID();
        IssueCommentResponse commentResponse = new IssueCommentResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "This is a comment");

        List<IssueCommentResponse> comments = List.of(commentResponse);

        when(issueCommentService.getIssueComments(issueId)).thenReturn(comments);

        mockMvc.perform(get("/api/issue-comments/v1/{issueId}", issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].comment").value("This is a comment"));
    }

    @Test
    void testSave() throws Exception {
        IssueCommentRequest request = new IssueCommentRequest(UUID.randomUUID(), UUID.randomUUID(), "This is a comment");
        IssueCommentResponse commentResponse = new IssueCommentResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "This is a comment");

        when(issueCommentService.create(any(IssueCommentRequest.class))).thenReturn(commentResponse);

        mockMvc.perform(post("/api/issue-comments/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.comment").value("This is a comment"));
    }

    @Test
    void testUpdate() throws Exception {
        UUID id = UUID.randomUUID();
        IssueCommentRequest request = new IssueCommentRequest(UUID.randomUUID(), UUID.randomUUID(), "Updated comment");
        IssueCommentResponse commentResponse = new IssueCommentResponse(id, UUID.randomUUID(), UUID.randomUUID(), "Updated comment");

        when(issueCommentService.update(any(UUID.class), any(IssueCommentRequest.class))).thenReturn(commentResponse);

        mockMvc.perform(put("/api/issue-comments/v1/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.comment").value("Updated comment"));
    }

    @Test
    void testDelete() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/issue-comments/v1/{id}", id))
                .andExpect(status().isNoContent());
    }
}