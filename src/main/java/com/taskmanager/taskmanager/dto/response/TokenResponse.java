package com.taskmanager.taskmanager.dto.response;

public record TokenResponse(
        UserResponse user,
        String accessToken,
        String refreshToken
) {
}
