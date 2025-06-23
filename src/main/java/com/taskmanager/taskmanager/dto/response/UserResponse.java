package com.taskmanager.taskmanager.dto.response;



import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.enums.Role;

import java.util.List;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    List<String> authorities
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNomUtilisateur(),
                user.getEmail(),
                user.getRole().stream()
                        .map(Role::getDisplayName)
                        .toList()
        );
    }
}
