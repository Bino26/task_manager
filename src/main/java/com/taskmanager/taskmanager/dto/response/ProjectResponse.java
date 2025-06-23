package com.taskmanager.taskmanager.dto.response;



import com.taskmanager.taskmanager.entity.Project;

import java.io.Serializable;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String nom,
        String description,
        String proprietaireName,
        String status
) implements Serializable {
    public static ProjectResponse from (Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getNom(),
                project.getDescription(),
                project.getProprietaireId().getNomUtilisateur(),
                project.getStatut().getDisplayName()
        );
    }
}
