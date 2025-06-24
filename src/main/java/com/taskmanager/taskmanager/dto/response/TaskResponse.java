package com.taskmanager.taskmanager.dto.response;

import com.taskmanager.taskmanager.entity.Task;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse implements Serializable {

    private UUID id;
    private String titre;
    private String description;
    private Instant dateEcheance;
    private Long heuresEstimees;
    private String statut;
    private String priorité;
    private UUID projetId;
    private UUID assigneId;

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitre(),
                task.getDescription(),
                task.getDateEcheance(),
                task.getHeuresEstimees() != null ? task.getHeuresEstimees() : 0L,
                task.getStatut().getDisplayName(), // Convert enum to readable name
                task.getPriorité().getDisplayName(), // Convert enum to readable name
                task.getProjetId() != null ? task.getProjetId().getId() : null,
                task.getAssigneId() != null ? task.getAssigneId().getId() : null
        );
    }
}
