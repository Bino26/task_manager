package com.taskmanager.taskmanager.unit.service;

import com.taskmanager.taskmanager.dto.request.UserRequest;
import com.taskmanager.taskmanager.dto.response.UserResponse;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.enums.Role;
import com.taskmanager.taskmanager.exception.custom.CustomAccessDeniedException;
import com.taskmanager.taskmanager.exception.custom.CustomAlreadyExistException;
import com.taskmanager.taskmanager.exception.custom.CustomNotFoundException;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.service.TokenService;
import com.taskmanager.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .nomUtilisateur("example")
                .email("example@example.com")
                .password("password")
                .role(new HashSet<>(List.of(Role.TEAM_MEMBER)))
                .build();
        user.setId(userId);
    }

    @Test
    void listAll_ShouldReturnUserList() {
        when(userRepository.findAllByDeletedAtNull()).thenReturn(List.of(user));
        List<UserResponse> users = userService.listAll();
        assertEquals(1, users.size());
        verify(userRepository).findAllByDeletedAtNull();
    }

    @Test
    void getById_ShouldReturnUser() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        UserResponse response = userService.getById(userId);
        assertEquals(user.getEmail(), response.email());
    }

    @Test
    void getById_ShouldThrowWhenNotFound() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.empty());
        assertThrows(CustomNotFoundException.class, () -> userService.getById(userId));
    }

    @Test
    void save_ShouldThrowIfEmailExists() {
        when(userRepository.existsByEmailAndDeletedAtNull(user.getEmail())).thenReturn(true);
        assertThrows(CustomAlreadyExistException.class, () -> userService.save(user));
    }

    @Test
    void save_ShouldSaveUserSuccessfully() {
        when(userRepository.existsByEmailAndDeletedAtNull(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserResponse response = userService.save(user);
        assertEquals(user.getEmail(), response.email());
        verify(userRepository).save(user);
    }

    @Test
    void update_ShouldUpdateUser() {
        UserRequest request = new UserRequest("Updated", "updated@example.com", "newpass");
        UserService spyService = Mockito.spy(userService);

        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        doReturn(user).when(spyService).getAuthenticatedUser();
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = spyService.update(userId, request);

        assertEquals("updated@example.com", response.email());
        assertEquals("Updated", response.name());
        verify(userRepository).save(user);
    }

    @Test
    void update_ShouldThrowIfUnauthorized() {
        UserRequest request = new UserRequest("Updated", "updated@example.com", "newpass");
        User anotherUser = User.builder().email("unauthorized@example.com").build();

        UserService spyService = Mockito.spy(userService);
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        doReturn(anotherUser).when(spyService).getAuthenticatedUser();

        assertThrows(CustomAccessDeniedException.class, () -> spyService.update(userId, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowIfEmailAlreadyExists() {
        UserRequest request = new UserRequest("Updated", "new@example.com", "newpass");

        UserService spyService = Mockito.spy(userService);
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        doReturn(user).when(spyService).getAuthenticatedUser();
        doThrow(new CustomAlreadyExistException("Email already exists")).when(spyService).existsByEmail(request.email());

        assertThrows(CustomAlreadyExistException.class, () -> spyService.update(userId, request));
    }

    @Test
    void addRole_ShouldAddRoleSuccessfully() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.addRole(userId, Role.TEAM_LEADER);

        assertTrue(response.authorities().contains(Role.TEAM_LEADER));
        verify(userRepository).save(user);
    }

    @Test
    void addRole_ShouldNotDuplicateRole() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        UserResponse response = userService.addRole(userId, Role.TEAM_MEMBER);

        assertEquals(1, response.authorities().size());
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeRole_ShouldRemoveRole() {
        user.getRole().add(Role.TEAM_LEADER);
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.removeRole(userId, Role.TEAM_LEADER);

        assertFalse(response.authorities().contains(Role.TEAM_LEADER));
    }

    @Test
    void removeRole_ShouldNotRemoveTeamMember() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        UserResponse response = userService.removeRole(userId, Role.TEAM_MEMBER);
        assertEquals(1, response.authorities().size());
    }

    @Test
    void removeRole_ShouldThrowIfRoleNotPresent() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        assertThrows(CustomNotFoundException.class, () -> userService.removeRole(userId, Role.TEAM_LEADER));
    }

    @Test
    void delete_ShouldSoftDeleteUser() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        userService.delete(userId);
        verify(userRepository).save(user);
        verify(tokenService).deleteRefreshTokenByUserId(userId);
    }

    @Test
    void getAuthenticatedUser_ShouldReturnUser() {
        user.setEmail("user@example.com");
        Authentication authentication = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmailAndDeletedAtNull(user.getEmail())).thenReturn(Optional.of(user));

        User result = userService.getAuthenticatedUser();

        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        when(userRepository.findByEmailAndDeletedAtNull(user.getEmail())).thenReturn(Optional.of(user));
        User found = userService.findByEmail(user.getEmail());
        assertEquals(user.getEmail(), found.getEmail());
    }

    @Test
    void findByEmail_ShouldThrowIfNotFound() {
        when(userRepository.findByEmailAndDeletedAtNull(user.getEmail())).thenReturn(Optional.empty());
        assertThrows(CustomNotFoundException.class, () -> userService.findByEmail(user.getEmail()));
    }
}
