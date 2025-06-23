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
import patika.defineX.dto.request.IssueAttachmentRequest;
import patika.defineX.dto.response.IssueAttachmentResponse;
import patika.defineX.service.IssueAttachmentService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class IssueAttachmentControllerTest {

    @Mock
    private IssueAttachmentService issueAttachmentService;

    @InjectMocks
    private IssueAttachmentController issueAttachmentController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(issueAttachmentController).build();
    }

    @Test
    void testGetIssueAttachment() throws Exception {
        UUID issueId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();
        IssueAttachmentResponse attachmentResponse = new IssueAttachmentResponse(attachmentId, issueId, "file.txt", "example.com/file.txt");
        List<IssueAttachmentResponse> attachments = List.of(attachmentResponse);

        when(issueAttachmentService.findByIssueId(issueId)).thenReturn(attachments);

        mockMvc.perform(get("/api/issue-attachment/v1/{issueId}", issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(attachmentId.toString()))
                .andExpect(jsonPath("$[0].issueId").value(issueId.toString()))
                .andExpect(jsonPath("$[0].fileName").value("file.txt"))
                .andExpect(jsonPath("$[0].url").value("example.com/file.txt"));
    }

    @Test
    void testCreateIssueAttachment() throws Exception {
        UUID issueId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();
        IssueAttachmentRequest request = new IssueAttachmentRequest(issueId, "file.txt", "example.com/file.txt");
        IssueAttachmentResponse response = new IssueAttachmentResponse(attachmentId, issueId, "file.txt", "example.com/file.txt");

        when(issueAttachmentService.save(any(IssueAttachmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/issue-attachment/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(attachmentId.toString()))
                .andExpect(jsonPath("$.issueId").value(issueId.toString()))
                .andExpect(jsonPath("$.fileName").value("file.txt"))
                .andExpect(jsonPath("$.url").value("example.com/file.txt"));
    }

    @Test
    void testDeleteIssueAttachment() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/issue-attachment/v1/{id}", id))
                .andExpect(status().isNoContent());
    }
}