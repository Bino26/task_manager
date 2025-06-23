package com.taskmanager.taskmanager.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import patika.defineX.dto.response.IssueHistoryResponse;
import patika.defineX.model.enums.IssueStatus;
import patika.defineX.service.IssueHistoryService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IssueHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IssueHistoryService issueHistoryService;

    @InjectMocks
    private IssueHistoryController issueHistoryController;

    private UUID issueId;
    private IssueHistoryResponse issueHistoryResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(issueHistoryController).build();

        issueId = UUID.randomUUID();
        issueHistoryResponse = new IssueHistoryResponse(
                UUID.randomUUID(),
                issueId,
                IssueStatus.IN_ANALYSIS,
                IssueStatus.IN_PROGRESS,
                UUID.randomUUID(),
                "Status updated",
                LocalDateTime.now()
        );
    }

    @Test
    void testList_WhenIssueExists_ShouldReturnIssueHistories() throws Exception {
        List<IssueHistoryResponse> historyList = List.of(issueHistoryResponse);

        when(issueHistoryService.listAllByIssueId(issueId)).thenReturn(historyList);

        mockMvc.perform(get("/api/issue-histories/v1/{issueId}", issueId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].issueId").value(issueId.toString()))
            .andExpect(jsonPath("$[0].previousStatus").value("IN_ANALYSIS"))
                .andExpect(jsonPath("$[0].newStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].reason").value("Status updated"));
    }

    @Test
    void testList_WhenNoIssueHistory_ShouldReturnEmptyList() throws Exception {
        when(issueHistoryService.listAllByIssueId(issueId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/issue-histories/v1/{issueId}", issueId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
}
