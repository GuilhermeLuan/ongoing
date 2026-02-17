package dev.guilhermeluan.ongoing.auth;

import dev.guilhermeluan.ongoing.auth.dto.AuthResponse;
import dev.guilhermeluan.ongoing.auth.dto.LoginRequest;
import dev.guilhermeluan.ongoing.auth.dto.RefreshRequest;
import dev.guilhermeluan.ongoing.auth.dto.RegisterRequest;
import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import dev.guilhermeluan.ongoing.user.RefreshToken;
import dev.guilhermeluan.ongoing.user.RefreshTokenRepository;
import dev.guilhermeluan.ongoing.user.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIT extends BaseIntegrationTest {

    public static final String AUTH_URL = "/api/v1/auth";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldReturn201_WhenDataIsValid() {
        RegisterRequest request = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );

        String response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(AUTH_URL + "/register")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("accessToken").isNotNull().isString();
        assertThatJson(response).node("refreshToken").isNotNull().isString();

        assertThat(userRepository.findByEmail("john@example.com")).isPresent();
    }

    @Test
    void register_ShouldReturn400_WhenEmailAlreadyExists() {
        RegisterRequest firstUser = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );
        authService.register(firstUser);

        RegisterRequest duplicateUser = new RegisterRequest(
                "Jane Doe",
                "john@example.com",
                "password456"
        );

        String response = given()
                .contentType(ContentType.JSON)
                .body(duplicateUser)
                .when().post(AUTH_URL + "/register")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains("Email already");
    }

    @Test
    void register_ShouldReturn400_WhenEmailIsInvalid() {
        RegisterRequest request = new RegisterRequest(
                "John Doe",
                "invalid-email",
                "password123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(AUTH_URL + "/register")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all();
    }

    @Test
    void register_ShouldReturn400_WhenPasswordIsTooShort() {
        RegisterRequest request = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "short"
        );

        String response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(AUTH_URL + "/register")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains("at least 8 characters");
    }

    @Test
    void register_ShouldReturn400_WhenNameIsBlank() {
        RegisterRequest request = new RegisterRequest(
                "",
                "john@example.com",
                "password123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(AUTH_URL + "/register")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all();
    }


    @Test
    void login_ShouldReturn200_WhenCredentialsAreCorrect() {
        RegisterRequest registerRequest = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest(
                "john@example.com",
                "password123"
        );

        String response = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post(AUTH_URL + "/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("accessToken").isNotNull().isString();
        assertThatJson(response).node("refreshToken").isNotNull().isString();
    }

    @Test
    void login_ShouldReturn401_WhenPasswordIsWrong() {
        RegisterRequest registerRequest = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest(
                "john@example.com",
                "3wrongpassword"
        );

        String response = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post(AUTH_URL + "/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains("Invalid");
    }

    @Test
    void login_ShouldReturn401_WhenEmailDoesNotExist() {
        LoginRequest loginRequest = new LoginRequest(
                "nonexistent@example.com",
                "password123"
        );

        String response = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post(AUTH_URL + "/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains("Invalid");
    }


    @Test
    void refresh_ShouldReturn200_WhenTokenIsValid() {
        RegisterRequest registerRequest = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );
        AuthResponse authResponse = authService.register(registerRequest);

        RefreshRequest refreshRequest = new RefreshRequest(authResponse.refreshToken());

        String response = given()
                .contentType(ContentType.JSON)
                .body(refreshRequest)
                .when().post(AUTH_URL + "/refresh")
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("accessToken").isNotNull().isString();
        assertThatJson(response).node("refreshToken").isNotNull().isString();

        assertThat(refreshTokenRepository.findByToken(authResponse.refreshToken())).isEmpty();
    }

    @Test
    void refresh_ShouldReturn401_WhenTokenDoesNotExist() {
        RefreshRequest refreshRequest = new RefreshRequest("non-existent-token");

        String response = given()
                .contentType(ContentType.JSON)
                .body(refreshRequest)
                .when().post(AUTH_URL + "/refresh")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains("Invalid");
    }

    @Test
    void refresh_ShouldReturn401_WhenTokenIsExpired() {
        // Cria usuário
        RegisterRequest registerRequest = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );
        AuthResponse authResponse = authService.register(registerRequest);

        // Busca o refresh token no banco e expira manualmente
        RefreshToken refreshToken = refreshTokenRepository.findByToken(authResponse.refreshToken()).orElseThrow();
        refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expira no passado
        refreshTokenRepository.save(refreshToken);

        // Tenta usar o token expirado
        RefreshRequest refreshRequest = new RefreshRequest(authResponse.refreshToken());

        String response = given()
                .contentType(ContentType.JSON)
                .body(refreshRequest)
                .when().post(AUTH_URL + "/refresh")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains("Invalid refresh token");

        // Verifica que o token expirado foi deletado
        assertThat(refreshTokenRepository.findByToken(authResponse.refreshToken())).isEmpty();
    }

    // ========== TESTES DE PROTEÇÃO DE ENDPOINTS ==========

    @Test
    void protectedEndpoint_ShouldReturn200_WhenTokenIsValid() {
        // Cria usuário e obtém token
        RegisterRequest registerRequest = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );
        AuthResponse authResponse = authService.register(registerRequest);

        // Acessa endpoint protegido com token válido
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authResponse.accessToken())
                .when().get("/api/v1/subscriptions")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void protectedEndpoint_ShouldReturn403_WhenNoTokenProvided() {
        // Acessa endpoint protegido sem token
        given()
                .contentType(ContentType.JSON)
                .when().get("/api/v1/subscriptions")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
