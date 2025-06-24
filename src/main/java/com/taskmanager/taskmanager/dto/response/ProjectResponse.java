package com.taskmanager.taskmanager.dto.response;

import com.taskmanager.taskmanager.entity.Project;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse implements Serializable {

    private UUID id;
    private String nom;
    private String description;
    private String proprietaireName;
    private String status;

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getNom(),
                project.getDescription(),
                project.getProprietaireId().getNomUtilisateur(),
                project.getStatut().getDisplayName()
        );
    }
}
