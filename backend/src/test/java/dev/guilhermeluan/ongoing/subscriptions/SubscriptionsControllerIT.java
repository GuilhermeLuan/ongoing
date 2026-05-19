// HU01 - Cadastrar Assinatura
package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.auth.AuthService;
import dev.guilhermeluan.ongoing.auth.dto.AuthResponse;
import dev.guilhermeluan.ongoing.auth.dto.RegisterRequest;
import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.RefreshTokenRepository;
import dev.guilhermeluan.ongoing.user.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionsControllerIT extends BaseIntegrationTest {

    public static final String API_URL = "/api/v1/subscriptions";

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private AuthService authService;

    private String authToken;

    @BeforeEach
    void setUpTestData() {
        subscriptionsRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest request = new RegisterRequest(
                "Test User",
                "test@example.com",
                "password123"
        );
        AuthResponse response = authService.register(request);

        this.authToken = response.accessToken();
    }

    // HU01 - CA1: persiste assinatura e calcula automaticamente a próxima data de cobrança
    @Test
    void create_ShouldCreateAndCalculateNextPaymentDate_WhenDataIsValid() {
        LocalDate startDate = LocalDate.now();
        SubscriptionRequestDto request = new SubscriptionRequestDto(
                "Netflix",
                "Netflix mensal",
                new BigDecimal("39.95"),
                startDate,
                null,
                true,
                true,
                Currency.BRL,
                null,
                1L,
                1L,
                BillingCycle.MONTHLY,
                1L
        );

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when().post(API_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("id").isNotNull().isNumber();
        assertThatJson(response).node("name").isEqualTo("Netflix");
        assertThatJson(response).node("value").isEqualTo(39.95);
        assertThatJson(response).node("nextPaymentDate").isEqualTo(startDate.plusMonths(1).toString());
    }

    // HU01 - CA2: aceita request com Moeda BRL e Data de Início igual à data atual (defaults do frontend)
    @Test
    void create_ShouldAcceptDefaults_WhenCurrencyIsBRLAndStartDateIsToday() {
        LocalDate today = LocalDate.now();
        SubscriptionRequestDto request = new SubscriptionRequestDto(
                "Spotify",
                "Spotify mensal",
                new BigDecimal("19.95"),
                today,
                null,
                true,
                true,
                Currency.BRL,
                null,
                1L,
                1L,
                BillingCycle.MONTHLY,
                1L
        );

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when().post(API_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("currency").isEqualTo("BRL");
        assertThatJson(response).node("startDate").isEqualTo(today.toString());
    }

    // HU01 - CA3: registra preferência de Lembrete de Renovação para notificação D-1 da próxima cobrança
    @Test
    void create_ShouldPersistNotifyFlag_WhenLembreteRenovacaoIsActive() {
        SubscriptionRequestDto request = new SubscriptionRequestDto(
                "Disney+",
                "Disney Plus mensal",
                new BigDecimal("27.90"),
                LocalDate.now(),
                null,
                true,
                true,
                Currency.BRL,
                null,
                1L,
                1L,
                BillingCycle.MONTHLY,
                1L
        );

        Long createdId = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when().post(API_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .log().all()
                .body("notifyUser", org.hamcrest.Matchers.equalTo(true))
                .extract().jsonPath().getLong("id");

        Subscriptions persisted = subscriptionsRepository.findById(createdId).orElseThrow();
        assertThat(persisted.getNotify()).isTrue();
    }

    // HU01 - CA4: bloqueia salvamento quando Nome ou Valor não atendem regras obrigatórias
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidNameOrValueRequests")
    void create_ShouldReturnBadRequest_WhenNameOrValueIsInvalid(String testName, SubscriptionRequestDto request, String expectedErrorMessage) {
        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when().post(API_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains(expectedErrorMessage);
    }

    private static Stream<Arguments> provideInvalidNameOrValueRequests() {
        LocalDate now = LocalDate.now();
        LocalDate nextMonth = now.plusMonths(1);

        return Stream.of(
                Arguments.of(
                        "Nome em branco",
                        new SubscriptionRequestDto("", "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, 1L, 1L, BillingCycle.MONTHLY, 1L),
                        "Name is required"
                ),
                Arguments.of(
                        "Nome nulo",
                        new SubscriptionRequestDto(null, "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, 1L, 1L, BillingCycle.MONTHLY, 1L),
                        "Name is required"
                ),
                Arguments.of(
                        "Valor nulo",
                        new SubscriptionRequestDto("Valid Name", "Description", null, now, nextMonth, true, true, Currency.BRL, null, 1L, 1L, BillingCycle.MONTHLY, 1L),
                        "Value is required"
                ),
                Arguments.of(
                        "Valor negativo",
                        new SubscriptionRequestDto("Valid Name", "Description", new BigDecimal("-10.00"), now, nextMonth, true, true, Currency.BRL, null, 1L, 1L, BillingCycle.MONTHLY, 1L),
                        "Value must be positive"
                ),
                Arguments.of(
                        "Valor zero",
                        new SubscriptionRequestDto("Valid Name", "Description", BigDecimal.ZERO, now, nextMonth, true, true, Currency.BRL, null, 1L, 1L, BillingCycle.MONTHLY, 1L),
                        "Value must be positive"
                )
        );
    }
}
