package com.taskmanager.taskmanager.unit.service;

import com.taskmanager.taskmanager.dto.response.TokenResponse;
import com.taskmanager.taskmanager.entity.RefreshToken;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.enums.Role;
import com.taskmanager.taskmanager.exception.custom.CustomNotFoundException;
import com.taskmanager.taskmanager.exception.custom.TokenExpiredException;
import com.taskmanager.taskmanager.repository.RefreshTokenRepository;
import com.taskmanager.taskmanager.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserDetails userDetails;

    private final int refreshExpirationTime = 86400000;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(refreshTokenRepository);
        tokenService.token = "mYiADZOy9Hq5pTfomhj9hPUJLGAN/ZB6AZ4yoHfCtN0f8DaNQr6+4qeaJ9YfMK4ZbX85J3ao6TotW+jrLKLGeafpaYYLn2mmrYOB/1mJzhQYEaf883PtWAIwnTwHpPuEsJAhyHwEtlTvy++pZA+0bns66RUDBk80bJUA4JhJUI8";
        tokenService.accessExpiration = 3600000;
        tokenService.refreshExpiration = refreshExpirationTime;
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken() {
        String username = "test@example.com";
        String token = tokenService.generateAccessToken(username);

        assertNotNull(token);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(tokenService.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(username, claims.getSubject());
    }

    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        String username = "test@example.com";
        String token = tokenService.generateAccessToken(username);

        when(userDetails.getUsername()).thenReturn(username);

        boolean isValid = tokenService.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForInvalidUser() {
        String username = "test@example.com";
        String token = tokenService.generateAccessToken(username);

        when(userDetails.getUsername()).thenReturn("invalid@example.com");

        boolean isValid = tokenService.validateToken(token, userDetails);

        assertFalse(isValid);
    }

    @Test
    void createRefreshToken_ShouldReturnValidToken() {
        User user = new User();
        user.setId(UUID.randomUUID());

        when(refreshTokenRepository.findByUserIdAndDeletedAtNull(user.getId())).thenReturn(null);

        String refreshToken = tokenService.createRefreshToken(user);

        assertNotNull(refreshToken);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_WhenValid_ShouldReturnNewTokens() {
        User user = User.builder()
                .nomUtilisateur("Test User")
                .email("test@example.com")
                .password("password")
                .role(Set.of(Role.TEAM_LEADER))
                .build();
        user.setId(UUID.randomUUID());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(new Date(System.currentTimeMillis() + refreshExpirationTime))
                .build();

        when(refreshTokenRepository.findByTokenAndDeletedAtNull(refreshToken.getToken()))
                .thenReturn(Optional.of(refreshToken));

        TokenResponse response = tokenService.validateRefreshToken(refreshToken.getToken());

        assertNotNull(response);
        assertNotNull(response.refreshToken());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_WhenTokenIsExpired_ShouldThrowException() {
        User user = User.builder()
                .nomUtilisateur("Test User")
                .email("test@example.com")
                .password("password")
                .role(Set.of(Role.TEAM_LEADER))
                .build();
        user.setId(UUID.randomUUID());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(new Date(System.currentTimeMillis() - refreshExpirationTime))
                .build();

        when(refreshTokenRepository.findByTokenAndDeletedAtNull(refreshToken.getToken()))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(TokenExpiredException.class,
                () -> tokenService.validateRefreshToken(refreshToken.getToken()));

        verify(refreshTokenRepository, times(0)).save(refreshToken);
    }

    @Test
    void deleteRefreshTokenByUserId_WhenTokenExists_ShouldSoftDeleteToken() {
        UUID userId = UUID.randomUUID();
        RefreshToken refreshToken = mock(RefreshToken.class);

        when(refreshTokenRepository.findByUserIdAndDeletedAtNull(userId)).thenReturn(refreshToken);

        tokenService.deleteRefreshTokenByUserId(userId);

        verify(refreshToken, times(1)).softDelete();
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void deleteRefreshTokenByUserId_WhenTokenDoesNotExist_ShouldNotDeleteToken() {
        UUID userId = UUID.randomUUID();

        when(refreshTokenRepository.findByUserIdAndDeletedAtNull(userId)).thenReturn(null);

        tokenService.deleteRefreshTokenByUserId(userId);

        verify(refreshTokenRepository, times(0)).save(any(RefreshToken.class));
    }

    @Test
    void deleteRefreshToken_WhenTokenExists_ShouldSoftDeleteToken() {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = mock(RefreshToken.class);

        when(refreshTokenRepository.findByTokenAndDeletedAtNull(token)).thenReturn(Optional.of(refreshToken));

        tokenService.deleteRefreshToken(token);

        verify(refreshToken, times(1)).softDelete();
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void deleteRefreshToken_WhenTokenDoesNotExist_ShouldThrowException() {
        String token = UUID.randomUUID().toString();

        when(refreshTokenRepository.findByTokenAndDeletedAtNull(token)).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> tokenService.deleteRefreshToken(token));

        verify(refreshTokenRepository, times(0)).save(any(RefreshToken.class));
    }
}
