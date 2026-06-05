package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.auth.AuthService;
import dev.guilhermeluan.ongoing.auth.dto.AuthResponse;
import dev.guilhermeluan.ongoing.auth.dto.RegisterRequest;
import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.entities.*;
import dev.guilhermeluan.ongoing.subscriptions.pricehistory.SubscriptionPriceHistory;
import dev.guilhermeluan.ongoing.subscriptions.pricehistory.SubscriptionPriceHistoryRepository;
import dev.guilhermeluan.ongoing.user.RefreshTokenRepository;
import dev.guilhermeluan.ongoing.user.User;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private SubscriptionPriceHistoryRepository subscriptionPriceHistoryRepository;


    private String authToken;
    private User authenticatedUser;

    @BeforeEach
    void setUpTestData() {
        subscriptionPriceHistoryRepository.deleteAll();
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
        this.authenticatedUser = userRepository.findByEmail(request.email()).orElseThrow();
    }


    @Test
    void findPriceHistoryById_ShouldReturnPriceHistoryOrderedByChangedAtDesc() {
        Subscriptions subscription = subscriptionsRepository.save(
                createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1), BillingCycle.MONTHLY, authenticatedUser)
        );

        SubscriptionPriceHistory olderHistory = subscriptionPriceHistoryRepository.save(
                SubscriptionPriceHistory.builder()
                        .subscription(subscription)
                        .user(authenticatedUser)
                        .oldValue(new BigDecimal("29.95"))
                        .newValue(new BigDecimal("39.95"))
                        .changePercentage(new BigDecimal("33.39"))
                        .isPriceSpike(true)
                        .changedAt(LocalDateTime.now().minusDays(2))
                        .build()
        );

        SubscriptionPriceHistory newerHistory = subscriptionPriceHistoryRepository.save(
                SubscriptionPriceHistory.builder()
                        .subscription(subscription)
                        .user(authenticatedUser)
                        .oldValue(new BigDecimal("39.95"))
                        .newValue(new BigDecimal("49.95"))
                        .changePercentage(new BigDecimal("25.03"))
                        .isPriceSpike(true)
                        .changedAt(LocalDateTime.now().minusDays(1))
                        .build()
        );

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL + "/{id}/price-history", subscription.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).isArray().hasSize(2);
        assertThatJson(response).node("[0].id").isEqualTo(newerHistory.getId());
        assertThatJson(response).node("[0].oldValue").isEqualTo(39.95);
        assertThatJson(response).node("[0].newValue").isEqualTo(49.95);
        assertThatJson(response).node("[0].isPriceSpike").isEqualTo(true);
        assertThatJson(response).node("[0].changedAt").isString();
        assertThatJson(response).node("[1].id").isEqualTo(olderHistory.getId());
        assertThatJson(response).node("[1].oldValue").isEqualTo(29.95);
        assertThatJson(response).node("[1].newValue").isEqualTo(39.95);

        List<Map<String, Object>> payload = io.restassured.path.json.JsonPath.from(response).getList("$");
        assertThat(payload.getFirst().keySet())
                .containsExactlyInAnyOrder("id", "subscriptionId", "oldValue", "newValue", "changePercentage", "isPriceSpike", "changedAt");
    }

    @Test
    void findPriceHistoryById_ShouldNotReturnHistoryFromOtherUsers() {
        Subscriptions subscription = subscriptionsRepository.save(
                createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1), BillingCycle.MONTHLY, authenticatedUser)
        );

        authService.register(new RegisterRequest("Other User", "other-history@example.com", "password123"));
        User otherUser = userRepository.findByEmail("other-history@example.com").orElseThrow();

        subscriptionPriceHistoryRepository.save(
                SubscriptionPriceHistory.builder()
                        .subscription(subscription)
                        .user(otherUser)
                        .oldValue(new BigDecimal("19.95"))
                        .newValue(new BigDecimal("39.95"))
                        .changePercentage(new BigDecimal("100.25"))
                        .isPriceSpike(true)
                        .changedAt(LocalDateTime.now().minusDays(1))
                        .build()
        );

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL + "/{id}/price-history", subscription.getId())
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        assertThatJson(response).isArray().isEmpty();
    }

    @Test
    void findPriceSpikes_ShouldReturnAuthenticatedUserSpikesWithinDateRangeOrderedByChangedAtDesc() {
        Subscriptions subscription = subscriptionsRepository.save(
                createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1), BillingCycle.MONTHLY, authenticatedUser)
        );

        authService.register(new RegisterRequest("Other User", "other-spike@example.com", "password123"));
        User otherUser = userRepository.findByEmail("other-spike@example.com").orElseThrow();
        Subscriptions otherSubscription = subscriptionsRepository.save(
                createSubscription("Spotify", new BigDecimal("19.95"), LocalDate.now(), LocalDate.now().plusMonths(1), BillingCycle.MONTHLY, otherUser)
        );

        SubscriptionPriceHistory olderSpike = subscriptionPriceHistoryRepository.save(
                createPriceHistory(subscription, authenticatedUser, "39.95", "49.95", "25.03", true, LocalDateTime.of(2026, 5, 10, 10, 0))
        );
        SubscriptionPriceHistory newerSpike = subscriptionPriceHistoryRepository.save(
                createPriceHistory(subscription, authenticatedUser, "49.95", "59.95", "20.02", true, LocalDateTime.of(2026, 5, 20, 10, 0))
        );
        subscriptionPriceHistoryRepository.save(
                createPriceHistory(subscription, authenticatedUser, "59.95", "61.95", "3.34", false, LocalDateTime.of(2026, 5, 21, 10, 0))
        );
        subscriptionPriceHistoryRepository.save(
                createPriceHistory(subscription, authenticatedUser, "29.95", "39.95", "33.39", true, LocalDateTime.of(2026, 4, 30, 10, 0))
        );
        subscriptionPriceHistoryRepository.save(
                createPriceHistory(otherSubscription, otherUser, "19.95", "29.95", "50.13", true, LocalDateTime.of(2026, 5, 22, 10, 0))
        );

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .queryParam("from", "2026-05-01")
                .queryParam("to", "2026-05-31")
                .when().get(API_URL + "/price-spikes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).isArray().hasSize(2);
        assertThatJson(response).node("[0].id").isEqualTo(newerSpike.getId());
        assertThatJson(response).node("[0].subscriptionId").isEqualTo(subscription.getId());
        assertThatJson(response).node("[0].oldValue").isEqualTo(49.95);
        assertThatJson(response).node("[0].newValue").isEqualTo(59.95);
        assertThatJson(response).node("[0].isPriceSpike").isEqualTo(true);
        assertThatJson(response).node("[0].changedAt").isEqualTo("2026-05-20T10:00:00");
        assertThatJson(response).node("[1].id").isEqualTo(olderSpike.getId());
        assertThatJson(response).node("[1].subscriptionId").isEqualTo(subscription.getId());

        List<Map<String, Object>> payload = io.restassured.path.json.JsonPath.from(response).getList("$");
        assertThat(payload.getFirst().keySet())
                .containsExactlyInAnyOrder("id", "subscriptionId", "oldValue", "newValue", "changePercentage", "isPriceSpike", "changedAt");
    }

    @Test
    void findPriceSpikes_ShouldUseLastThirtyDaysAsDefaultRange() {
        Subscriptions subscription = subscriptionsRepository.save(
                createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1), BillingCycle.MONTHLY, authenticatedUser)
        );

        SubscriptionPriceHistory includedSpike = subscriptionPriceHistoryRepository.save(
                createPriceHistory(subscription, authenticatedUser, "39.95", "49.95", "25.03", true, LocalDateTime.now().minusDays(29))
        );
        subscriptionPriceHistoryRepository.save(
                createPriceHistory(subscription, authenticatedUser, "29.95", "39.95", "33.39", true, LocalDateTime.now().minusDays(31))
        );

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL + "/price-spikes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).isArray().hasSize(1);
        assertThatJson(response).node("[0].id").isEqualTo(includedSpike.getId());
    }

    @Test
    void findAll_ShouldReturnAllSubscriptions() {
        insertSampleSubscriptions();

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("totalElements").isEqualTo(2);
        assertThatJson(response).node("content").isArray().hasSize(2);
        assertThatJson(response).node("content[0].id").isNotNull().isNumber();
        assertThatJson(response).node("content[0].name").isEqualTo("Netflix");
        assertThatJson(response).node("content[0].description").isEqualTo("Netflix mensal");
        assertThatJson(response).node("content[0].value").isEqualTo(39.95);
        assertThatJson(response).node("content[0].categoryName").isEqualTo("Video Streaming");
        assertThatJson(response).node("content[0].paymentMethodName").isEqualTo("Credit Card");

        assertThatJson(response).node("content[1].id").isNotNull().isNumber();
        assertThatJson(response).node("content[1].name").isEqualTo("Spotify");
        assertThatJson(response).node("content[1].description").isEqualTo("Spotify mensal");
        assertThatJson(response).node("content[1].value").isEqualTo(19.95);
        assertThatJson(response).node("content[1].categoryName").isEqualTo("Video Streaming");
        assertThatJson(response).node("content[1].paymentMethodName").isEqualTo("Credit Card");
    }

    @Test
    void findById_ShouldReturnOneSubscriptionById() {
        Category videoStreaming = Category.builder().id(1L).build();
        PaymentMethod paymentMethod = PaymentMethod.builder().id(1L).build();
        Subscriptions subscription = subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), true, videoStreaming, paymentMethod));


        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL + "/{id}", subscription.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();
        assertThatJson(response).node("id").isNotNull().isEqualTo(subscription.getId());
        assertThatJson(response).node("name").isEqualTo(subscription.getName());
        assertThatJson(response).node("description").isEqualTo(subscription.getDescription());
        assertThatJson(response).node("value").isEqualTo(subscription.getValue());
        assertThatJson(response).node("categoryName").isNotNull();
        assertThatJson(response).node("paymentMethodName").isNotNull();

    }

    @Test
    void findById_ShouldThrowNotFoundException() {
        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
                        new SubscriptionRequestDto("", "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, BillingCycle.MONTHLY, null),
                        "Name is required"
                ),
                Arguments.of(
                        "Name is null",
                        new SubscriptionRequestDto(null, "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, BillingCycle.MONTHLY, null),
                        "Name is required"
                ),
                Arguments.of(
                        "Name exceeds 255 characters",
                        new SubscriptionRequestDto("A".repeat(256), "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, BillingCycle.MONTHLY, null),
                        "Name must be at most 255 characters"
                ),
                Arguments.of(
                        "Description exceeds 255 characters",
                        new SubscriptionRequestDto("Valid Name", "D".repeat(256), new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, BillingCycle.MONTHLY, null),
                        "Description must be at most 255 characters"
                ),
                Arguments.of(
                        "Value is null",
                        new SubscriptionRequestDto("Valid Name", "Description", null, now, nextMonth, true, true, Currency.BRL, null, null, null, BillingCycle.MONTHLY, null),
                        "Value is required"
                ),
                Arguments.of(
                        "Value is negative",
                        new SubscriptionRequestDto("Valid Name", "Description", new BigDecimal("-10.00"), now, nextMonth, true, true, Currency.BRL, null, null, null, BillingCycle.MONTHLY, null),
                        "Value must be positive"
                ),
                Arguments.of(
                        "Value is zero",
                        new SubscriptionRequestDto("Valid Name", "Description", BigDecimal.ZERO, now, nextMonth, true, true, Currency.BRL, null, null, null, BillingCycle.MONTHLY, null),
                        "Value must be positive"
                ),
                Arguments.of(
                        "Start date is null",
                        new SubscriptionRequestDto("Valid Name", "Description", new BigDecimal("10.00"), null, nextMonth, true, true, Currency.BRL, null, null, null, BillingCycle.MONTHLY, null),
                        "Start date is required"
                ),
                Arguments.of(
                        "Logo URL exceeds 255 characters",
                        new SubscriptionRequestDto("Valid Name", "Description", new BigDecimal("10.00"), now, nextMonth, true, true, Currency.BRL, "L".repeat(256), null, null, BillingCycle.MONTHLY, null),
                        "Logo URL must be at most 255 characters"
                ),
                Arguments.of(
                        "BillingCycle is null",
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
                BillingCycle.MONTHLY,
                1L
        );
    }

    @Test
    void delete_ShouldDeleteSubscription() {
        Subscriptions subscription = createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1), BillingCycle.MONTHLY, authenticatedUser);

        subscriptionsRepository.save(subscription);


        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
        BigDecimal previousValue = subscription.getValue();

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
                BillingCycle.YEARLY,
                1L
        );

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
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

        var priceHistory = subscriptionPriceHistoryRepository.findAll();
        assertThat(priceHistory).hasSize(1);
        assertThat(priceHistory.getFirst().getOldValue()).isEqualByComparingTo(previousValue);
        assertThat(priceHistory.getFirst().getNewValue()).isEqualByComparingTo(new BigDecimal("89.95"));
        assertThat(priceHistory.getFirst().getIsPriceSpike()).isTrue();
    }

    @Test
    void update_ShouldNotCreatePriceHistory_WhenValueIsUnchanged() {
        List<Subscriptions> subscriptions = insertSampleSubscriptions();
        Subscriptions subscription = subscriptions.getFirst();

        SubscriptionRequestDto updateRequest = new SubscriptionRequestDto(
                "Netflix Premium",
                "Novo nome sem alterar valor",
                subscription.getValue(),
                subscription.getStartDate(),
                subscription.getNextPaymentDate(),
                true,
                true,
                Currency.BRL,
                null,
                1L,
                1L,
                BillingCycle.MONTHLY,
                1L
        );

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(updateRequest)
                .when().put(API_URL + "/{id}", subscription.getId())
                .then()
                .statusCode(HttpStatus.OK.value());

        assertThat(subscriptionPriceHistoryRepository.findAll()).isEmpty();
    }

    @Test
    void update_ShouldMarkPriceSpike_WhenIncreaseIsGreaterOrEqualTenPercent() {
        List<Subscriptions> subscriptions = insertSampleSubscriptions();
        Subscriptions subscription = subscriptions.getFirst();

        SubscriptionRequestDto updateRequest = new SubscriptionRequestDto(
                subscription.getName(),
                subscription.getDescription(),
                new BigDecimal("44.00"),
                subscription.getStartDate(),
                subscription.getNextPaymentDate(),
                true,
                true,
                Currency.BRL,
                null,
                1L,
                1L,
                BillingCycle.MONTHLY,
                1L
        );

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(updateRequest)
                .when().put(API_URL + "/{id}", subscription.getId())
                .then()
                .statusCode(HttpStatus.OK.value());

        var priceHistory = subscriptionPriceHistoryRepository.findAll();
        assertThat(priceHistory).hasSize(1);
        assertThat(priceHistory.getFirst().getOldValue()).isEqualByComparingTo(new BigDecimal("39.95"));
        assertThat(priceHistory.getFirst().getNewValue()).isEqualByComparingTo(new BigDecimal("44.00"));
        assertThat(priceHistory.getFirst().getChangePercentage()).isEqualByComparingTo(new BigDecimal("10.14"));
        assertThat(priceHistory.getFirst().getIsPriceSpike()).isTrue();
    }

    @Test
    void update_ShouldChangeRelationships_WhenCategoryAndPaymentMethodChange() {
        // Arrange: Create subscription with category 1 and payment method 1
        Subscriptions subscription = subscriptionsRepository.save(
                Subscriptions.builder()
                        .name("Netflix")
                        .description("Netflix Basic")
                        .value(new BigDecimal("39.90"))
                        .startDate(LocalDate.now())
                        .nextPaymentDate(LocalDate.now().plusMonths(1))
                        .billingCycle(BillingCycle.MONTHLY)
                        .currency(Currency.BRL)
                        .notify(true)
                        .active(true)
                        .category(Category.builder().id(1L).build())  // Category 1
                        .paymentMethod(dev.guilhermeluan.ongoing.subscriptions.entities.PaymentMethod.builder().id(1L).build())  // Payment Method 1
                        .subscriptionType(dev.guilhermeluan.ongoing.subscriptions.entities.SubscriptionType.builder().id(1L).build())
                        .user(authenticatedUser)
                        .build()
        );

        // Act: Update to category 2 and payment method 2 (CHANGES!)
        SubscriptionRequestDto updateRequest = new SubscriptionRequestDto(
                "Netflix Premium",
                "Netflix Premium Plan",
                new BigDecimal("55.90"),
                subscription.getStartDate(),
                subscription.getNextPaymentDate(),
                true,
                true,
                Currency.BRL,
                null,
                2L,  // Change category from 1 to 2
                2L,  // Change payment method from 1 to 2
                BillingCycle.MONTHLY,
                1L
        );

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(updateRequest)
                .when().put(API_URL + "/{id}", subscription.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        // Assert: Verify the response
        assertThatJson(response).node("id").isNotNull().isNumber();
        assertThatJson(response).node("name").isEqualTo("Netflix Premium");
        assertThatJson(response).node("categoryId").isEqualTo(2);
        assertThatJson(response).node("paymentMethodId").isEqualTo(2);

        // Assert: Verify in database that relationships actually changed
        Subscriptions updatedSubscription = subscriptionsRepository.findById(subscription.getId()).orElseThrow();
        assertThat(updatedSubscription.getName()).isEqualTo("Netflix Premium");
        assertThat(updatedSubscription.getCategory()).isNotNull();
        assertThat(updatedSubscription.getCategory().getId()).isEqualTo(2L);  // Changed from 1 to 2
        assertThat(updatedSubscription.getPaymentMethod()).isNotNull();
        assertThat(updatedSubscription.getPaymentMethod().getId()).isEqualTo(2L);  // Changed from 1 to 2
        assertThat(updatedSubscription.getValue()).isEqualByComparingTo(new BigDecimal("55.90"));
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenSubscriptionIsNotFound() {

        long id = 99L;

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().delete(API_URL + "/{id}", id)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .log().all()
                .extract().body().asString();
    }

    private List<Subscriptions> insertSampleSubscriptions() {
        Category category = Category.builder().id(1L).build();
        PaymentMethod paymentMethod = PaymentMethod.builder().id(1L).build();

        Subscriptions subscriptions = subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1), BillingCycle.MONTHLY, authenticatedUser, category, paymentMethod));
        Subscriptions subscriptions1 = subscriptionsRepository.save(createSubscription("Spotify", new BigDecimal("19.95"), LocalDate.now().minusDays(10), LocalDate.now().plusMonths(1), BillingCycle.MONTHLY, authenticatedUser, category, paymentMethod));

        return List.of(subscriptions, subscriptions1);
    }

    private Subscriptions createSubscription(String name, BigDecimal value, LocalDate startDate, LocalDate nextPaymentDate, BillingCycle billingCycle, User user, Category category, PaymentMethod paymentMethod) {
        return Subscriptions.builder()
                .name(name)
                .description(name + " mensal")
                .value(value)
                .startDate(startDate)
                .nextPaymentDate(nextPaymentDate)
                .billingCycle(billingCycle)
                .currency(Currency.BRL)
                .category(category)
                .paymentMethod(paymentMethod)
                .notify(true)
                .active(true)
                .user(user)
                .build();
    }

    private Subscriptions createSubscription(String name, BigDecimal value, LocalDate startDate, LocalDate nextPaymentDate, BillingCycle billingCycle, User user) {
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
                .user(user)
                .build();
    }

    private SubscriptionPriceHistory createPriceHistory(
            Subscriptions subscription,
            User user,
            String oldValue,
            String newValue,
            String changePercentage,
            boolean isPriceSpike,
            LocalDateTime changedAt
    ) {
        return SubscriptionPriceHistory.builder()
                .subscription(subscription)
                .user(user)
                .oldValue(new BigDecimal(oldValue))
                .newValue(new BigDecimal(newValue))
                .changePercentage(new BigDecimal(changePercentage))
                .isPriceSpike(isPriceSpike)
                .changedAt(changedAt)
                .build();
    }

    private Subscriptions createSubscription(String name, BigDecimal value, boolean active, Category category) {
        return Subscriptions.builder()
                .name(name)
                .description(name + " mensal")
                .value(value)
                .startDate(LocalDate.now())
                .nextPaymentDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .currency(Currency.BRL)
                .notify(true)
                .active(active)
                .category(category)
                .user(authenticatedUser)
                .build();
    }

    private Subscriptions createSubscription(String name, BigDecimal value, boolean active, Category category, PaymentMethod paymentMethod) {
        return Subscriptions.builder()
                .name(name)
                .description(name + " mensal")
                .value(value)
                .startDate(LocalDate.now())
                .nextPaymentDate(LocalDate.now().plusMonths(1))
                .billingCycle(BillingCycle.MONTHLY)
                .currency(Currency.BRL)
                .notify(true)
                .active(active)
                .category(category)
                .user(authenticatedUser)
                .paymentMethod(paymentMethod)
                .build();
    }

    @Test
    void findAll_ShouldFilterByName_CaseInsensitivePartialMatch() {
        subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), true, null));
        subscriptionsRepository.save(createSubscription("Spotify", new BigDecimal("19.95"), true, null));
        subscriptionsRepository.save(createSubscription("Amazon Prime", new BigDecimal("14.90"), true, null));

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .queryParam("name", "net")
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        assertThatJson(response).node("totalElements").isEqualTo(1);
        assertThatJson(response).node("content[0].name").isEqualTo("Netflix");
    }

    @Test
    void findAll_ShouldFilterByActiveStatus() {
        subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), true, null));
        subscriptionsRepository.save(createSubscription("Spotify", new BigDecimal("19.95"), false, null));
        subscriptionsRepository.save(createSubscription("Amazon Prime", new BigDecimal("14.90"), true, null));

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .queryParam("active", false)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        assertThatJson(response).node("totalElements").isEqualTo(1);
        assertThatJson(response).node("content[0].name").isEqualTo("Spotify");
    }

    @Test
    void findAll_ShouldFilterByCategoryId() {
        Category videoStreaming = Category.builder().id(1L).build();
        Category musicStreaming = Category.builder().id(2L).build();

        subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), true, videoStreaming));
        subscriptionsRepository.save(createSubscription("Spotify", new BigDecimal("19.95"), true, musicStreaming));
        subscriptionsRepository.save(createSubscription("Disney+", new BigDecimal("27.90"), true, videoStreaming));

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .queryParam("categoryId", 1)
                .when().get(API_URL)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        assertThatJson(response).node("totalElements").isEqualTo(2);
        assertThatJson(response).node("content").isArray().hasSize(2);
    }

    @Test
    void findAll_ShouldFilterByCombinedParameters() {
        Category videoStreaming = Category.builder().id(1L).build();
        Category musicStreaming = Category.builder().id(2L).build();

        subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), true, videoStreaming));
        subscriptionsRepository.save(createSubscription("Spotify", new BigDecimal("19.95"), true, musicStreaming));
        subscriptionsRepository.save(createSubscription("Disney+", new BigDecimal("27.90"), false, videoStreaming));
        subscriptionsRepository.save(createSubscription("Amazon Prime", new BigDecimal("14.90"), true, videoStreaming));

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .queryParam("name", "net")
                .queryParam("active", true)
                .queryParam("categoryId", 1)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        assertThatJson(response).node("totalElements").isEqualTo(1);
        assertThatJson(response).node("content[0].name").isEqualTo("Netflix");
    }

    @Test
    void findAll_ShouldReturnAllSubscriptions_WhenNoFiltersProvided() {
        subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), true, null));
        subscriptionsRepository.save(createSubscription("Spotify", new BigDecimal("19.95"), false, null));
        subscriptionsRepository.save(createSubscription("Amazon Prime", new BigDecimal("14.90"), true, null));

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        assertThatJson(response).node("totalElements").isEqualTo(3);
        assertThatJson(response).node("content").isArray().hasSize(3);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenSubscriptionIsNotFound() {
        long id = 99L;

        var subscription = createSubscriptionRequestDTO();

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
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
                    "billingCycle": "MONTHLY"
                }
                """;

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
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
                    "billingCycle": "MONTHLY"
                }
                """;

        String response = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(invalidRequest)
                .when().put(API_URL + "/{id}", subscriptions.getFirst().getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all()
                .extract().body().asString();

        assertThat(response).contains("Invalid request body");
    }

    @Test
    void findAll_ShouldReturn403_WhenNoTokenProvided() {
        given()
                .contentType(ContentType.JSON)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void findById_ShouldReturn403_WhenNoTokenProvided() {
        given()
                .contentType(ContentType.JSON)
                .when().get(API_URL + "/{id}", 1)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void create_ShouldReturn403_WhenNoTokenProvided() {
        given()
                .contentType(ContentType.JSON)
                .body(createSubscriptionRequestDTO())
                .when().post(API_URL)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void update_ShouldReturn403_WhenNoTokenProvided() {
        given()
                .contentType(ContentType.JSON)
                .body(createSubscriptionRequestDTO())
                .when().put(API_URL + "/{id}", 1)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void delete_ShouldReturn403_WhenNoTokenProvided() {
        given()
                .contentType(ContentType.JSON)
                .when().delete(API_URL + "/{id}", 1)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }


    @Test
    void findAll_ShouldNotReturnSubscriptionsFromOtherUsers() {
        subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), true, null));

        authService.register(new RegisterRequest("Other User", "other@example.com", "password123"));
        User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();

        Subscriptions otherSubscription = subscriptionsRepository.save(
                Subscriptions.builder()
                        .name("Spotify")
                        .description("Spotify mensal")
                        .value(new BigDecimal("19.95"))
                        .startDate(LocalDate.now())
                        .nextPaymentDate(LocalDate.now().plusMonths(1))
                        .billingCycle(BillingCycle.MONTHLY)
                        .currency(Currency.BRL)
                        .active(true)
                        .notify(true)
                        .user(otherUser)
                        .build()
        );

        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        assertThatJson(response).node("totalElements").isEqualTo(1);
        assertThatJson(response).node("content[0].name").isEqualTo("Netflix");
    }

    @Test
    void findById_ShouldReturn404_WhenSubscriptionBelongsToOtherUser() {
        authService.register(new RegisterRequest("Other", "other@example.com", "password123"));
        User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();

        Subscriptions otherSubscription = subscriptionsRepository.save(
                Subscriptions.builder()
                        .name("Spotify")
                        .value(new BigDecimal("19.95"))
                        .startDate(LocalDate.now())
                        .nextPaymentDate(LocalDate.now().plusMonths(1))
                        .billingCycle(BillingCycle.MONTHLY)
                        .currency(Currency.BRL)
                        .active(true)
                        .user(otherUser)
                        .build()
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL + "/{id}", otherSubscription.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void delete_ShouldReturn404_WhenSubscriptionBelongsToOtherUser() {
        authService.register(new RegisterRequest("Other", "other@example.com", "password123"));
        User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();

        Subscriptions otherSubscription = subscriptionsRepository.save(
                Subscriptions.builder()
                        .name("Spotify")
                        .value(new BigDecimal("19.95"))
                        .startDate(LocalDate.now())
                        .nextPaymentDate(LocalDate.now().plusMonths(1))
                        .billingCycle(BillingCycle.MONTHLY)
                        .currency(Currency.BRL)
                        .active(true)
                        .user(otherUser)
                        .build()
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().delete(API_URL + "/{id}", otherSubscription.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(subscriptionsRepository.findById(otherSubscription.getId())).isPresent();
    }

    @Test
    void update_ShouldReturn404_WhenSubscriptionBelongsToOtherUser() {
        authService.register(new RegisterRequest("Other", "other@example.com", "password123"));
        User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();

        Subscriptions otherSubscription = subscriptionsRepository.save(
                Subscriptions.builder()
                        .name("Spotify")
                        .value(new BigDecimal("19.95"))
                        .startDate(LocalDate.now())
                        .nextPaymentDate(LocalDate.now().plusMonths(1))
                        .billingCycle(BillingCycle.MONTHLY)
                        .currency(Currency.BRL)
                        .active(true)
                        .user(otherUser)
                        .build()
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createSubscriptionRequestDTO())
                .when().put(API_URL + "/{id}", otherSubscription.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

}
