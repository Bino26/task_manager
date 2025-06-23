package com.taskmanager.taskmanager.dto.response;



import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.entity.enums.Role;

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
                user.getName(),
                user.getEmail(),
                user.getAuthorities().stream()
                        .map(Role::getDisplayName)
                        .toList()
        );
    }
}
