package dev.guilhermeluan.ongoing.dashboard;

import dev.guilhermeluan.ongoing.auth.AuthService;
import dev.guilhermeluan.ongoing.auth.dto.AuthResponse;
import dev.guilhermeluan.ongoing.auth.dto.RegisterRequest;
import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import dev.guilhermeluan.ongoing.exchange.ExchangeRateClient;
import dev.guilhermeluan.ongoing.exchange.dto.ExchangeRateResponse;
import dev.guilhermeluan.ongoing.subscriptions.SubscriptionsRepository;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Category;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.RefreshTokenRepository;
import dev.guilhermeluan.ongoing.user.User;
import dev.guilhermeluan.ongoing.user.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.Mockito.when;

class DashboardControllerIT extends BaseIntegrationTest {

    public static final String API_URL = "/api/v1/dashboard";

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private AuthService authService;

    @MockitoBean
    private ExchangeRateClient exchangeRateClient;

    private String authToken;
    private User authenticatedUser;

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
        this.authenticatedUser = userRepository.findByEmail(request.email()).orElseThrow();

        // Configure default mock for ExchangeRateClient
        when(exchangeRateClient.fetchRates()).thenReturn(
                new ExchangeRateResponse(
                        "USD",
                        LocalDate.now().toString(),
                        Map.of(
                                "BRL", new BigDecimal("5.0"),
                                "USD", BigDecimal.ONE,
                                "EUR", new BigDecimal("1.1")
                        )
                )
        );
    }

    @Test
    void getDashboard_ShouldReturn200WithDashboard_WhenUserHasSubscriptions() {
        Category videoStreaming = Category.builder().id(1L).name("Video Streaming").build();
        Category musicStreaming = Category.builder().id(2L).name("Music Streaming").build();

        createSubscription("Netflix", new BigDecimal("49.90"), Currency.BRL, BillingCycle.MONTHLY,
                videoStreaming, LocalDate.now().plusDays(15), authenticatedUser);
        createSubscription("Spotify", new BigDecimal("19.90"), Currency.BRL, BillingCycle.MONTHLY,
                musicStreaming, LocalDate.now().plusDays(10), authenticatedUser);

        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("spendingByCategory").isArray().isNotEmpty();
        assertThatJson(response).node("monthlyAverage").isNumber().isGreaterThan(BigDecimal.ZERO);
        assertThatJson(response).node("currency").isEqualTo("BRL");
        assertThatJson(response).node("exchangeRateDate").isNotNull();
    }

    @Test
    void getDashboard_ShouldReturnEmptyDashboard_WhenNoActiveSubscriptions() {
        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("spendingByCategory").isArray().isEmpty();
        assertThatJson(response).node("monthlyAverage").isEqualTo(new BigDecimal("0.00"));
        assertThatJson(response).node("thisMonthTotal").isEqualTo(new BigDecimal("0.00"));
        assertThatJson(response).node("yearlyTotal").isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void getDashboard_ShouldReturn403_WhenNoTokenProvided() {
        given()
                .contentType(ContentType.JSON)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void getDashboard_ShouldOnlyShowAuthenticatedUserSubscriptions() {
        Category videoStreaming = Category.builder().id(1L).name("Video Streaming").build();

        // Create subscription for authenticated user
        createSubscription("Netflix", new BigDecimal("49.90"), Currency.BRL, BillingCycle.MONTHLY,
                videoStreaming, LocalDate.now().plusDays(15), authenticatedUser);

        // Create another user with subscriptions
        AuthResponse otherUserResponse = authService.register(
                new RegisterRequest("Other User", "other@example.com", "password123")
        );
        User otherUser = userRepository.findByEmail("other@example.com").orElseThrow();

        createSubscription("Spotify", new BigDecimal("100.00"), Currency.BRL, BillingCycle.MONTHLY,
                videoStreaming, LocalDate.now().plusDays(10), otherUser);

        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // Should return approximately 49.90 monthly, not including otherUser's 100.00
        assertThatJson(response).node("monthlyAverage").isEqualTo(new BigDecimal("49.90"));
        assertThatJson(response).node("yearlyTotal").isEqualTo(new BigDecimal("598.80")); // 49.90 * 12
    }

    @Test
    void getDashboard_ShouldConvertUsdToBrl() {
        Category gaming = Category.builder().id(3L).name("Gaming").build();

        // Uses default mock from @BeforeEach: USD to BRL = 5.0
        createSubscription("Xbox Game Pass", new BigDecimal("10.00"), Currency.USD, BillingCycle.MONTHLY,
                gaming, LocalDate.now().plusDays(20), authenticatedUser);

        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // 10.00 USD * 5.0 = 50.00 BRL monthly
        assertThatJson(response).node("monthlyAverage").isEqualTo(new BigDecimal("50.00"));
        assertThatJson(response).node("yearlyTotal").isEqualTo(new BigDecimal("600.00")); // 50.00 * 12
    }

    @Test
    void getDashboard_ShouldConvertEurToBrl() {
        Category productivity = Category.builder().id(4L).name("Productivity").build();

        // Uses default mock from @BeforeEach: EUR to BRL = 5.0 / 1.1 ≈ 4.545454
        createSubscription("Notion", new BigDecimal("10.00"), Currency.EUR, BillingCycle.MONTHLY,
                productivity, LocalDate.now().plusDays(5), authenticatedUser);

        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // 10.00 EUR * (5.0 / 1.1) = 10.00 * 4.545454... ≈ 45.45 BRL monthly
        assertThatJson(response).node("monthlyAverage").isEqualTo(new BigDecimal("45.45"));
        assertThatJson(response).node("yearlyTotal").isEqualTo(new BigDecimal("545.40")); // 45.45 * 12
    }

    @Test
    void getDashboard_ShouldGroupSpendingByCategory() {
        Category videoStreaming = Category.builder().id(1L).name("Video Streaming").build();
        Category musicStreaming = Category.builder().id(2L).name("Music Streaming").build();

        // Two subscriptions in Video Streaming
        createSubscription("Netflix", new BigDecimal("49.90"), Currency.BRL, BillingCycle.MONTHLY,
                videoStreaming, LocalDate.now().plusDays(15), authenticatedUser);
        createSubscription("Disney+", new BigDecimal("27.90"), Currency.BRL, BillingCycle.MONTHLY,
                videoStreaming, LocalDate.now().plusDays(20), authenticatedUser);

        // One subscription in Music Streaming
        createSubscription("Spotify", new BigDecimal("19.90"), Currency.BRL, BillingCycle.MONTHLY,
                musicStreaming, LocalDate.now().plusDays(10), authenticatedUser);

        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        assertThatJson(response).node("spendingByCategory").isArray().hasSize(2);

        // Video Streaming total: (49.90 + 27.90) = 77.80 monthly
        // Music Streaming total: 19.90 monthly
        assertThatJson(response).node("spendingByCategory[0].total").isEqualTo(new BigDecimal("77.80"));
        assertThatJson(response).node("spendingByCategory[1].total").isEqualTo(new BigDecimal("19.90"));
    }

    @Test
    void getDashboard_ShouldOrderCategoriesByTotalDescending() {
        Category videoStreaming = Category.builder().id(1L).name("Video Streaming").build();
        Category musicStreaming = Category.builder().id(2L).name("Music Streaming").build();
        Category gaming = Category.builder().id(3L).name("Gaming").build();

        // Different amounts to test ordering
        createSubscription("Spotify", new BigDecimal("19.90"), Currency.BRL, BillingCycle.MONTHLY,
                musicStreaming, LocalDate.now().plusDays(10), authenticatedUser);
        createSubscription("Netflix", new BigDecimal("49.90"), Currency.BRL, BillingCycle.MONTHLY,
                videoStreaming, LocalDate.now().plusDays(15), authenticatedUser);
        createSubscription("Xbox", new BigDecimal("35.00"), Currency.BRL, BillingCycle.MONTHLY,
                gaming, LocalDate.now().plusDays(20), authenticatedUser);

        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // First category should have highest total (Netflix: 49.90 monthly)
        // Last category should have lowest total (Spotify: 19.90 monthly)
        assertThatJson(response).node("spendingByCategory[0].total").isEqualTo(new BigDecimal("49.90"));
        assertThatJson(response).node("spendingByCategory[1].total").isEqualTo(new BigDecimal("35.00"));
        assertThatJson(response).node("spendingByCategory[2].total").isEqualTo(new BigDecimal("19.90"));
    }

    @Test
    void getDashboard_ShouldCalculateThisMonthTotal_OnlyForCurrentMonth() {
        Category videoStreaming = Category.builder().id(1L).name("Video Streaming").build();

        LocalDate now = LocalDate.now();
        LocalDate thisMonth = now.withDayOfMonth(15);
        LocalDate nextMonth = now.plusMonths(1).withDayOfMonth(15);

        // Subscription with next payment this month
        createSubscription("Netflix", new BigDecimal("49.90"), Currency.BRL, BillingCycle.MONTHLY,
                videoStreaming, thisMonth, authenticatedUser);

        // Subscription with next payment next month
        createSubscription("Spotify", new BigDecimal("19.90"), Currency.BRL, BillingCycle.MONTHLY,
                videoStreaming, nextMonth, authenticatedUser);

        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // Only Netflix (49.90) should be in thisMonthTotal
        assertThatJson(response).node("thisMonthTotal").isEqualTo(new BigDecimal("49.90"));
        // But both should be in yearlyTotal and monthlyAverage
        assertThatJson(response).node("monthlyAverage").isEqualTo(new BigDecimal("69.80")); // 49.90 + 19.90
    }

    @Test
    void getDashboard_ShouldCalculateYearlyTotal_WithMixedBillingCycles() {
        Category videoStreaming = Category.builder().id(1L).name("Video Streaming").build();

        // Monthly subscription: 50.00 * 12 = 600.00
        createSubscription("Netflix", new BigDecimal("50.00"), Currency.BRL, BillingCycle.MONTHLY,
                videoStreaming, LocalDate.now().plusDays(15), authenticatedUser);

        // Yearly subscription: 120.00 * 1 = 120.00
        createSubscription("Amazon Prime", new BigDecimal("120.00"), Currency.BRL, BillingCycle.YEARLY,
                videoStreaming, LocalDate.now().plusDays(30), authenticatedUser);

        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // Yearly total: 600.00 + 120.00 = 720.00
        assertThatJson(response).node("yearlyTotal").isEqualTo(new BigDecimal("720.00"));
    }

    @Test
    void getDashboard_ShouldCalculateMonthlyAverage_WithYearlySubscription() {
        Category videoStreaming = Category.builder().id(1L).name("Video Streaming").build();

        // Yearly subscription: 120.00 / 12 = 10.00 monthly
        createSubscription("Amazon Prime", new BigDecimal("120.00"), Currency.BRL, BillingCycle.YEARLY,
                videoStreaming, LocalDate.now().plusDays(30), authenticatedUser);

        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when().get(API_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        // Monthly average: 120.00 / 12 = 10.00
        assertThatJson(response).node("monthlyAverage").isEqualTo(new BigDecimal("10.00"));
        assertThatJson(response).node("yearlyTotal").isEqualTo(new BigDecimal("120.00"));
    }

    private Subscriptions createSubscription(String name, BigDecimal value, Currency currency,
                                             BillingCycle billingCycle, Category category,
                                             LocalDate nextPaymentDate, User user) {
        Subscriptions subscription = Subscriptions.builder()
                .name(name)
                .description(name + " subscription")
                .value(value)
                .startDate(LocalDate.now())
                .nextPaymentDate(nextPaymentDate)
                .billingCycle(billingCycle)
                .currency(currency)
                .notify(true)
                .active(true)
                .category(category)
                .user(user)
                .build();

        return subscriptionsRepository.save(subscription);
    }
}
