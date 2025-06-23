package com.taskmanager.taskmanager.dto.request;

import com.taskmanager.taskmanager.entity.Project;
import com.taskmanager.taskmanager.enums.Status;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectRequest(
        @NotNull
        UUID proprietaireId,
        @NotBlank
        String description,
        @NotNull
        Status statut,
        @NotBlank
        LocalDateTime dateDebut
) {
    public static Project from (ProjectRequest projectRequest) {
        return Project.builder()
                .description(projectRequest.description())
                .statut(projectRequest.statut())
                .dateDebut(projectRequest.dateDebut())
                .build();
    }
}
