package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByIdAndDeletedAtNull(UUID id);
    Page<Project> findAllWithPaginationByProprietaireId_IdAndDeletedAtNull(UUID proprietaireId, Pageable pageable);
    List<Project> findAllByProprietaireId_IdAndDeletedAtNull(UUID proprietaireId);
    boolean existsByNomAndDeletedAtNull(String name);
}
