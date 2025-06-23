package com.taskmanager.taskmanager.unit.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.taskmanager.taskmanager.dto.request.LoginRequest;
import com.taskmanager.taskmanager.dto.request.RefreshTokenRequest;
import com.taskmanager.taskmanager.dto.request.UserRequest;
import com.taskmanager.taskmanager.dto.response.TokenResponse;
import com.taskmanager.taskmanager.dto.response.UserResponse;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.enums.Role;
import com.taskmanager.taskmanager.service.AuthService;
import com.taskmanager.taskmanager.service.TokenService;
import com.taskmanager.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .nomUtilisateur("John Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(new HashSet<>(Set.of(Role.TEAM_LEADER)))
                .build();
    }

    @Test
    void register_ShouldSaveUserAndReturnResponse() {
        UserRequest request = new UserRequest("John Doe", "john.doe@example.com", "password");
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userService.save(any(User.class))).thenReturn(UserResponse.from(user));

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(user.getNomUtilisateur(), response.name());
        verify(userService).save(any(User.class));
    }

    @Test
    void login_ShouldAuthenticateUserAndReturnToken() {
        LoginRequest request = new LoginRequest("john.doe@example.com", "password");
        when(userService.findByEmail(request.email())).thenReturn(user);
        when(tokenService.generateAccessToken(user.getEmail())).thenReturn("accessToken");
        when(tokenService.createRefreshToken(user)).thenReturn("refreshToken");

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertEquals("refreshToken", response.refreshToken());
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("john.doe@example.com", "wrongPassword");
        when(userService.findByEmail(request.email())).thenReturn(user);
        doThrow(new BadCredentialsException("Invalid email or password"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refresh_ShouldReturnNewToken_WhenRefreshTokenIsValid() {
        RefreshTokenRequest request = new RefreshTokenRequest("validRefreshToken");
        when(tokenService.validateRefreshToken(request.refreshToken())).thenReturn(new TokenResponse(UserResponse.from(user), "newAccessToken", "newRefreshToken"));

        TokenResponse response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("newAccessToken", response.accessToken());
    }

    @Test
    void logout_ShouldDeleteRefreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("validRefreshToken");
        doNothing().when(tokenService).deleteRefreshToken(request.refreshToken());

        authService.logout(request);

        verify(tokenService).deleteRefreshToken(request.refreshToken());
    }
}
