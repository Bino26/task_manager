package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.request.LoginRequest;
import com.taskmanager.taskmanager.dto.request.RefreshTokenRequest;
import com.taskmanager.taskmanager.dto.request.UserRequest;
import com.taskmanager.taskmanager.dto.response.TokenResponse;
import com.taskmanager.taskmanager.dto.response.UserResponse;
import com.taskmanager.taskmanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            method = "POST",
            summary = "Login",
            description = "Login with 'admin@mail.com' and 'passss' for being able to access all endpoints"
    )
    @PostMapping("/v1/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Operation(
            method = "POST",
            summary = "Register",
            description = "Register with email and password"
    )
    @PostMapping("/v1/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(userRequest));
    }

    @Operation(
            method = "POST",
            summary = "Refresh token",
            description = "Refresh your access token with refresh token"
    )
    @PostMapping("/v1/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(
            method = "POST",
            summary = "Logout",
            description = "Logout with refresh token"
    )
    @PostMapping("/v1/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
