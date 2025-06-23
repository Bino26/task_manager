package com.taskmanager.taskmanager.unit.service;

import com.taskmanager.taskmanager.dto.response.TaskResponse;
import com.taskmanager.taskmanager.entity.Task;
import com.taskmanager.taskmanager.enums.PriorityLevel;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.exception.custom.OverdueTaskNotFoundException;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listOverdueTasks_shouldReturnOverdueTasks() {
        // Arrange
        Task task = Task.builder()
                .titre("Late Task")
                .description("Overdue task description")
                .dateEcheance(Instant.now().minusSeconds(3600)) // in the past
                .priorité(PriorityLevel.HIGH)
                .statut(Status.TO_DO)
                .build();
        task.setId(UUID.randomUUID());

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task));

        when(taskRepository.findOverdueTasks(pageable)).thenReturn(taskPage);

        // Act
        Page<TaskResponse> result = taskService.listOverdueTasks(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Late Task", result.getContent().get(0).titre());
        verify(taskRepository).findOverdueTasks(pageable);
    }

    @Test
    void listOverdueTasks_shouldThrowException_whenNoOverdueTasks() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(taskRepository.findOverdueTasks(pageable)).thenReturn(Page.empty());

        // Act & Assert
        OverdueTaskNotFoundException exception = assertThrows(
                OverdueTaskNotFoundException.class,
                () -> taskService.listOverdueTasks(pageable)
        );

        assertEquals("No overdue tasks found.", exception.getMessage());
        verify(taskRepository).findOverdueTasks(pageable);
    }
}