
package com.taskmanager.taskmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.taskmanager.dto.request.LoginRequest;
import com.taskmanager.taskmanager.dto.request.RefreshTokenRequest;
import com.taskmanager.taskmanager.dto.request.UserRequest;
import com.taskmanager.taskmanager.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebTestClient webTestClient;

    private UserRequest userRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest("testuser@mail.com", "password123", Set.of(Role.TEAM_MEMBER).toString());
        loginRequest = new LoginRequest("testuser@mail.com", "password123");
    }

    @Test
    void register_ShouldReturn201() {
        webTestClient.post()
                .uri("/api/auth/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.email").isEqualTo("testuser@mail.com");
    }

    @Test
    void login_ShouldReturnTokenResponse() {
        webTestClient.post().uri("/api/auth/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest).exchange();

        webTestClient.post()
                .uri("/api/auth/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.refreshToken").isNotEmpty();
    }

    @Test
    void refreshToken_ShouldReturnNewAccessToken() {
        webTestClient.post().uri("/api/auth/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest).exchange();

        var tokenResponse = webTestClient.post()
                .uri("/api/auth/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenContainer.class)
                .returnResult().getResponseBody();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(tokenResponse.refreshToken);

        webTestClient.post()
                .uri("/api/auth/v1/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshTokenRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty();
    }

    @Test
    void logout_ShouldReturnNoContent() {

        webTestClient.post().uri("/api/auth/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest).exchange();

        var tokenResponse = webTestClient.post()
                .uri("/api/auth/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenContainer.class)
                .returnResult().getResponseBody();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(tokenResponse.refreshToken);

        webTestClient.post()
                .uri("/api/auth/v1/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshTokenRequest)
                .exchange()
                .expectStatus().isNoContent();
    }


    static class TokenContainer {
        public String accessToken;
        public String refreshToken;
    }
}
