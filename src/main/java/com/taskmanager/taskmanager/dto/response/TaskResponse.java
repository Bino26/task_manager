package com.taskmanager.taskmanager.dto.response;

import com.taskmanager.taskmanager.enums.PriorityLevel;
import com.taskmanager.taskmanager.enums.Status;
import com.taskmanager.taskmanager.entity.Task;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String titre,
        String description,
        Instant dateEcheance,
        Long heuresEstimees,
        Status statut,
        PriorityLevel priorité,
        UUID projetId,
        UUID assigneId
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitre(),
                task.getDescription(),
                task.getDateEcheance(),
                task.getHeuresEstimees(),
                task.getStatut(),
                task.getPriorité(),
                task.getProjetId() != null ? task.getProjetId().getId() : null,
                task.getAssigneId() != null ? task.getAssigneId().getId() : null
        );
    }
}
