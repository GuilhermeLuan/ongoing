// HU02 - Realizar Logon | HU03 - Realizar Cadastro
package dev.guilhermeluan.ongoing.auth;

import dev.guilhermeluan.ongoing.auth.dto.LoginRequest;
import dev.guilhermeluan.ongoing.auth.dto.RegisterRequest;
import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import dev.guilhermeluan.ongoing.user.RefreshTokenRepository;
import dev.guilhermeluan.ongoing.user.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

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

    // HU03 - CA1: cadastro com sucesso ao fornecer dados válidos
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

    // HU03 - CA2: erro quando e-mail já está cadastrado na base
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

    // HU03 - CA2: erro quando senha não atende requisitos mínimos de segurança
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

    // HU03 - CA3: bloqueia envio quando e-mail tem formato inválido
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

    // HU03 - CA3: bloqueia envio quando campo Nome está em branco
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

    // HU02 - CA1: login com sucesso com credenciais válidas, retornando tokens de acesso
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

    // HU02 - CA2: erro quando senha não confere com a cadastrada
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

    // HU02 - CA2: erro quando e-mail informado não está cadastrado
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
}
