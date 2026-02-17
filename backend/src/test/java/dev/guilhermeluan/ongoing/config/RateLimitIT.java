package dev.guilhermeluan.ongoing.config;

import dev.guilhermeluan.ongoing.auth.AuthService;
import dev.guilhermeluan.ongoing.auth.dto.AuthResponse;
import dev.guilhermeluan.ongoing.auth.dto.LoginRequest;
import dev.guilhermeluan.ongoing.auth.dto.RegisterRequest;
import dev.guilhermeluan.ongoing.user.RefreshTokenRepository;
import dev.guilhermeluan.ongoing.user.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

class RateLimitIT extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void authRateLimit_ShouldReturn429_AfterExceedingLimit() {
        LoginRequest loginRequest = new LoginRequest("test@test.com", "wrongpassword");

        // Primeiras 5 requests não devem ser bloqueadas pelo rate limit
        for (int i = 0; i < 5; i++) {
            int status = given()
                    .contentType(ContentType.JSON)
                    .body(loginRequest)
                    .when().post("/api/v1/auth/login")
                    .then()
                    .extract().statusCode();

            assertThat(status).isNotEqualTo(429);
        }

        // 6ª request deve ser bloqueada pelo rate limit
        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/api/v1/auth/login")
                .then()
                .statusCode(429);
    }

    @Test
    void apiRateLimit_ShouldReturn429_AfterExceedingLimit() {
        RegisterRequest registerRequest = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );
        AuthResponse authResponse = authService.register(registerRequest);

        // Primeiras 60 requests devem passar
        for (int i = 0; i < 60; i++) {
            int status = given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + authResponse.accessToken())
                    .when().get("/api/v1/subscriptions")
                    .then()
                    .extract().statusCode();

            assertThat(status).isNotEqualTo(429);
        }

        // 61ª request deve ser bloqueada pelo rate limit
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authResponse.accessToken())
                .when().get("/api/v1/subscriptions")
                .then()
                .statusCode(429);
    }
}
