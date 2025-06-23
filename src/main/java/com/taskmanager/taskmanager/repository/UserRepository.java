package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByDeletedAtNull();
    Optional<User> findByIdAndDeletedAtNull(UUID id);
    boolean existsByEmailAndDeletedAtNull(String email);
    Optional<User> findByEmailAndDeletedAtNull(String username);
}
