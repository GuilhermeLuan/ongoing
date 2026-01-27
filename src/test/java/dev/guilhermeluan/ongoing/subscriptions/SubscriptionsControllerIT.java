package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
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
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionsControllerIT extends BaseIntegrationTest {


    public static final String API_URL = "/api/v1/subscriptions";

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;

    @BeforeEach
    void cleanDatabase() {
        subscriptionsRepository.deleteAll();
    }

    @Test
    void findAll_ShouldReturnAllSubscriptions() {
        insertSampleSubscriptions();

        String response = given().contentType(ContentType.JSON)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).isArray().hasSize(2);
        assertThatJson(response).node("[0].id").isNotNull().isNumber();
        assertThatJson(response).node("[0].name").isEqualTo("Netflix");
        assertThatJson(response).node("[0].description").isEqualTo("Netflix mensal");
        assertThatJson(response).node("[0].value").isEqualTo(39.95);

        assertThatJson(response).node("[1].id").isNotNull().isNumber();
        assertThatJson(response).node("[1].name").isEqualTo("Spotify");
        assertThatJson(response).node("[1].description").isEqualTo("Spotify mensal");
        assertThatJson(response).node("[1].value").isEqualTo(19.95);
    }

    @Test
    void findById_ShouldReturnOneSubscriptionById() {
        var subscription = insertSampleSubscriptions().getFirst();

        String response = given().contentType(ContentType.JSON)
                .when().get(API_URL + "/{id}", subscription.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();
        assertThatJson(response).node("id").isNotNull().isEqualTo(subscription.getId());
        assertThatJson(response).node("name").isEqualTo(subscription.getName());
        assertThatJson(response).node("description").isEqualTo(subscription.getDescription());
        assertThatJson(response).node("value").isEqualTo(subscription.getValue());
    }

    @Test
    void findById_ShouldThrowNotFoundException() {
        given().contentType(ContentType.JSON)
                .when().get(API_URL + "/{id}", 99)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .log().all()
                .extract().body().asString();
    }

    @Test
    void create_ShouldCreateANewSubscription() {
        SubscriptionRequestDto request = createSubscriptionRequestDTO();

        var response = given().contentType(ContentType.JSON)
                .body(request)
                .when().post(API_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .log().all()
                .extract().body().asString();


        assertThatJson(response).node("id").isNotNull().isNumber();
        assertThatJson(response).node("name").isEqualTo(request.name());
        assertThatJson(response).node("description").isEqualTo(request.description());
        assertThatJson(response).node("value").isEqualTo(request.value());
    }

    private static Stream<Arguments> provideInvalidSubscriptionRequests() {
        LocalDate now = LocalDate.now();
        LocalDate nextMonth = now.plusMonths(1);

        return Stream.of(
                Arguments.of(
                        "Name is blank",
                        new SubscriptionRequestDto("", "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, 1L, null),
                        "Name is required"
                ),
                Arguments.of(
                        "Name is null",
                        new SubscriptionRequestDto(null, "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, 1L, null),
                        "Name is required"
                ),
                Arguments.of(
                        "Name exceeds 255 characters",
                        new SubscriptionRequestDto("A".repeat(256), "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, 1L, null),
                        "Name must be at most 255 characters"
                ),
                Arguments.of(
                        "Description exceeds 255 characters",
                        new SubscriptionRequestDto("Valid Name", "D".repeat(256), new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, 1L, null),
                        "Description must be at most 255 characters"
                ),
                Arguments.of(
                        "Value is null",
                        new SubscriptionRequestDto("Valid Name", "Description", null, now, nextMonth, true, true, Currency.BRL, null, null, null, 1L, null),
                        "Value is required"
                ),
                Arguments.of(
                        "Value is negative",
                        new SubscriptionRequestDto("Valid Name", "Description", new BigDecimal("-10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, 1L, null),
                        "Value must be positive"
                ),
                Arguments.of(
                        "Value is zero",
                        new SubscriptionRequestDto("Valid Name", "Description", BigDecimal.ZERO, now, nextMonth, true, true, Currency.BRL, null, null, null, 1L, null),
                        "Value must be positive"
                ),
                Arguments.of(
                        "Start date is null",
                        new SubscriptionRequestDto("Valid Name", "Description", new BigDecimal("10.00"), null, nextMonth, true, true, Currency.BRL, null, null, null, 1L, null),
                        "Start date is required"
                ),
                Arguments.of(
                        "Next payment date is null",
                        new SubscriptionRequestDto("Valid Name", "Description", new BigDecimal("10.00"), now, null, true, true, Currency.BRL, null, null, null, 1L, null),
                        "Next payment date is required"
                ),
                Arguments.of(
                        "Logo URL exceeds 255 characters",
                        new SubscriptionRequestDto("Valid Name", "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, "L".repeat(256), null, null, 1L, null),
                        "Logo URL must be at most 255 characters"
                ),
                Arguments.of(
                        "BillingCycleId is null",
                        new SubscriptionRequestDto("Valid Name", "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, null, null),
                        "BillingCycle is required"
                )
        );
    }

    private static SubscriptionRequestDto createSubscriptionRequestDTO() {
        return new SubscriptionRequestDto(
                "Netflix",
                "Netflix mensal",
                new BigDecimal("39.95"),
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                true,
                true,
                Currency.BRL,
                null,
                1L,
                1L,
                1L,
                1L
        );
    }

    @Test
    void delete_ShouldDeleteSubscription() {
        Subscriptions subscription = createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1), BillingCycle.builder().id(1L).build());

        subscriptionsRepository.save(subscription);


        given().contentType(ContentType.JSON)
                .when().delete(API_URL + "/{id}", subscription.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .log().all()
                .extract().body().asString();

        assertThat(subscriptionsRepository.findAll()).isEmpty();
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidSubscriptionRequests")
    void create_ShouldReturnBadRequest_WhenValidationFails(String testName, SubscriptionRequestDto request, String expectedErrorMessage) {
        String response = given().contentType(ContentType.JSON)
                .body(request)
                .when().post(API_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains(expectedErrorMessage);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidSubscriptionRequests")
    void update_ShouldReturnBadRequest_WhenValidationFails(String testName, SubscriptionRequestDto request, String expectedErrorMessage) {
        List<Subscriptions> subscriptions = insertSampleSubscriptions();

        String response = given().contentType(ContentType.JSON)
                .body(request)
                .when().put(API_URL + "/{id}", subscriptions.getFirst().getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains(expectedErrorMessage);
    }

    @Test
    void update_ShouldUpdatedSubscriptions() {
        List<Subscriptions> subscriptions = insertSampleSubscriptions();

        Subscriptions subscription = subscriptions.getFirst();

        SubscriptionRequestDto updateRequest = new SubscriptionRequestDto(
                "Amazon Prime",
                "Amazon Prime anual",
                new BigDecimal("89.95"),
                subscription.getStartDate(),
                subscription.getNextPaymentDate(),
                true,
                true,
                Currency.BRL,
                null,
                1L,
                1L,
                1L,
                1L
        );

        String response = given().contentType(ContentType.JSON)
                .body(updateRequest)
                .when().put(API_URL + "/{id}", subscription.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("id").isNotNull().isNumber();
        assertThatJson(response).node("name").isEqualTo("Amazon Prime");
        assertThatJson(response).node("description").isEqualTo("Amazon Prime anual");
        assertThatJson(response).node("value").isEqualTo(89.95);

        var subscriptionUpdated = subscriptionsRepository.findById(subscription.getId()).orElseThrow();
        assertThat(subscriptionUpdated.getName()).isEqualTo("Amazon Prime");
        assertThat(subscriptionUpdated.getDescription()).isEqualTo("Amazon Prime anual");
        assertThat(subscriptionUpdated.getValue()).isEqualByComparingTo(new BigDecimal("89.95"));
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenSubscriptionIsNotFound() {

        long id = 99L;

        given().contentType(ContentType.JSON)
                .when().delete(API_URL + "/{id}", id)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .log().all()
                .extract().body().asString();
    }

    private List<Subscriptions> insertSampleSubscriptions() {
        Subscriptions subscriptions = subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1), BillingCycle.builder().id(1L).build()));
        Subscriptions subscriptions1 = subscriptionsRepository.save(createSubscription("Spotify", new BigDecimal("19.95"), LocalDate.now().minusDays(10), LocalDate.now().plusMonths(1), BillingCycle.builder().id(1L).build()));

        return List.of(subscriptions, subscriptions1);
    }

    private Subscriptions createSubscription(String name, BigDecimal value, LocalDate startDate, LocalDate nextPaymentDate, BillingCycle billingCycle) {
        return Subscriptions.builder()
                .name(name)
                .description(name + " mensal")
                .value(value)
                .startDate(startDate)
                .nextPaymentDate(nextPaymentDate)
                .billingCycle(billingCycle)
                .currency(Currency.BRL)
                .notify(true)
                .active(true)
                .build();
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenSubscriptionIsNotFound() {
        long id = 99L;

        var subscription = createSubscriptionRequestDTO();

        given().contentType(ContentType.JSON)
                .body(subscription)
                .when().put(API_URL + "/{id}", id)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .log().all()
                .extract().body().asString();
    }

    @Test
    void create_ShouldReturnBadRequest_WhenCurrencyIsInvalid() {
        String invalidRequest = """
                {
                    "name": "Netflix",
                    "description": "Netflix mensal",
                    "value": 39.95,
                    "startDate": "2026-01-01",
                    "nextPaymentDate": "2026-02-01",
                    "active": true,
                    "notifyUser": true,
                    "currency": "INVALID",
                    "billingCycleId": 1
                }
                """;

        String response = given().contentType(ContentType.JSON)
                .body(invalidRequest)
                .when().post(API_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains("Invalid request body");
    }

    @Test
    void update_ShouldReturnBadRequest_WhenCurrencyIsInvalid() {
        List<Subscriptions> subscriptions = insertSampleSubscriptions();

        String invalidRequest = """
                {
                    "name": "Netflix",
                    "description": "Netflix mensal",
                    "value": 39.95,
                    "startDate": "2026-01-01",
                    "nextPaymentDate": "2026-02-01",
                    "active": true,
                    "notifyUser": true,
                    "currency": "XYZ",
                    "billingCycleId": 1
                }
                """;

        String response = given().contentType(ContentType.JSON)
                .body(invalidRequest)
                .when().put(API_URL + "/{id}", subscriptions.getFirst().getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains("Invalid request body");
    }

}