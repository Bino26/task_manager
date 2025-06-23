package com.taskmanager.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.taskmanager.taskmanager.enums.PriorityLevel;
import com.taskmanager.taskmanager.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Duration;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Instant dateEcheance;

    @Column(nullable = false)
    private Long heuresEstimees;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status statut = Status.TO_DO;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PriorityLevel priorité;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Project projetId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigne_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User assigneId;

    @PrePersist
    public void prePersist() {
        if (this.dateEcheance != null) {
            Instant now = Instant.now();
            long heures = Duration.between(now, dateEcheance).toHours();
            this.heuresEstimees = Math.max(heures, 0); // avoid negatives
        }
    }
}
