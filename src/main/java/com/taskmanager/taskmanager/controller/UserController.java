package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.request.UserRequest;
import com.taskmanager.taskmanager.dto.response.UserResponse;
import com.taskmanager.taskmanager.enums.Role;
import com.taskmanager.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilisateurs")
@Tag(name = "User", description = "User API")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @Operation(
            method = "GET",
            summary = "List all users",
            description = "List all users in the system"
    )
    @GetMapping("/v1")
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(userService.listAll());
    }

    @Operation(
            method = "POST",
            summary = "Create a user",
            description = "Create a user"
    )
    @GetMapping("/v1/{id}")
    public ResponseEntity<UserResponse> getUserById (@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(
            method = "POST",
            summary = "Update a user",
            description = "Update a user"
    )
    @PutMapping("/v1/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id,@Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.update(id, userRequest));
    }

    @Operation(
            method = "POST",
            summary = "Add role to a user",
            description = "Add role to a user, only project managers can add roles"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PatchMapping("/v1/{id}/roles/add")
    public ResponseEntity<UserResponse> addRole(@PathVariable UUID id, @RequestParam Role role) {
        return ResponseEntity.ok(userService.addRole(id, role));
    }

    @Operation(
            method = "POST",
            summary = "Remove role from a user",
            description = "Remove role from a user, only project managers can remove roles and cannot remove the team member role of a user"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PatchMapping("/v1/{id}/roles/remove")
    public ResponseEntity<UserResponse> removeRole(@PathVariable UUID id, @RequestParam Role role) {
        return ResponseEntity.ok(userService.removeRole(id, role));
    }

    @Operation(
            method = "DELETE",
            summary = "Delete a user",
            description = "Delete a user"
    )
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
