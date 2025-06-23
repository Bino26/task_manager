package com.taskmanager.taskmanager.dto.request;

import com.taskmanager.taskmanager.entity.Task;
import com.taskmanager.taskmanager.enums.PriorityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record TaskRequest(
        @NotBlank
        String titre,

        @NotBlank
        String description,

        @NotNull
        Instant dateEcheance,

        @NotNull
        PriorityLevel priorité,

        @NotNull
        UUID projetId,

        @NotNull
        UUID assigneId
) {
    public static Task.TaskBuilder from(TaskRequest taskRequest) {
        return Task.builder()
                .titre(taskRequest.titre())
                .description(taskRequest.description())
                .dateEcheance(taskRequest.dateEcheance())
                .priorité(taskRequest.priorité());
    }
}
