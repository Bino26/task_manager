package com.taskmanager.taskmanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank
    @Size(min = 3, max = 50)
    String name,
    @NotBlank
    @Email
    String email,
    @NotBlank
    @Size(min = 6, max = 50)
    String password
) {
}
