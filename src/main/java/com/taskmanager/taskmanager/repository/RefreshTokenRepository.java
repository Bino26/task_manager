package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenAndDeletedAtNull(String refreshToken);
    RefreshToken findByUserIdAndDeletedAtNull(UUID id);
}
