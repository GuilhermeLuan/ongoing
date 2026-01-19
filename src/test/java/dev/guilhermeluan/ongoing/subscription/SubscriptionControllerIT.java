package dev.guilhermeluan.ongoing.subscription;

import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.hamcrest.Matchers.*;

class SubscriptionControllerIT extends BaseIntegrationTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @BeforeEach
    void cleanDatabase() {
        subscriptionRepository.deleteAll();
    }

    @Test
    void shouldCreateSubscription() {
        String requestBody = """
                {
                    "name": "Netflix",
                    "description": "Streaming service",
                    "price": 15.99,
                    "billingCycle": "MONTHLY",
                    "nextBillingDate": "2026-02-19",
                    "isActive": true
                }
                """;

        String response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/subscriptions")
                .then()
                .statusCode(201)
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("id").isNotNull();
        assertThatJson(response).node("name").isEqualTo("Netflix");
        assertThatJson(response).node("description").isEqualTo("Streaming service");
        assertThatJson(response).node("price").isEqualTo(15.99);
        assertThatJson(response).node("billingCycle").isEqualTo("MONTHLY");
        assertThatJson(response).node("nextBillingDate").isEqualTo("2026-02-19");
        assertThatJson(response).node("isActive").isEqualTo(true);
        assertThatJson(response).node("createdAt").isNotNull();
        assertThatJson(response).node("updatedAt").isNotNull();
    }

    @Test
    void shouldGetAllSubscriptions() {
        // Create test subscriptions
        Subscription subscription1 = Subscription.builder()
                .name("Netflix")
                .description("Streaming service")
                .price(new BigDecimal("15.99"))
                .billingCycle("MONTHLY")
                .nextBillingDate(LocalDate.of(2026, 2, 19))
                .isActive(true)
                .build();

        Subscription subscription2 = Subscription.builder()
                .name("Spotify")
                .description("Music streaming")
                .price(new BigDecimal("9.99"))
                .billingCycle("MONTHLY")
                .nextBillingDate(LocalDate.of(2026, 2, 20))
                .isActive(true)
                .build();

        subscriptionRepository.save(subscription1);
        subscriptionRepository.save(subscription2);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/subscriptions")
                .then()
                .statusCode(200)
                .log().all()
                .body("size()", equalTo(2))
                .body("[0].name", notNullValue())
                .body("[1].name", notNullValue());
    }

    @Test
    void shouldGetSubscriptionById() {
        // Create test subscription
        Subscription subscription = Subscription.builder()
                .name("Netflix")
                .description("Streaming service")
                .price(new BigDecimal("15.99"))
                .billingCycle("MONTHLY")
                .nextBillingDate(LocalDate.of(2026, 2, 19))
                .isActive(true)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        String response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/subscriptions/" + saved.getId())
                .then()
                .statusCode(200)
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("id").isEqualTo(saved.getId());
        assertThatJson(response).node("name").isEqualTo("Netflix");
        assertThatJson(response).node("description").isEqualTo("Streaming service");
        assertThatJson(response).node("price").isEqualTo(15.99);
    }

    @Test
    void shouldReturn404WhenSubscriptionNotFound() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/subscriptions/999")
                .then()
                .statusCode(404)
                .log().all()
                .body(containsString("Subscription not found with id: 999"));
    }

    @Test
    void shouldUpdateSubscription() {
        // Create test subscription
        Subscription subscription = Subscription.builder()
                .name("Netflix")
                .description("Streaming service")
                .price(new BigDecimal("15.99"))
                .billingCycle("MONTHLY")
                .nextBillingDate(LocalDate.of(2026, 2, 19))
                .isActive(true)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        String requestBody = """
                {
                    "name": "Netflix Premium",
                    "description": "Premium streaming service",
                    "price": 19.99,
                    "billingCycle": "MONTHLY",
                    "nextBillingDate": "2026-03-19",
                    "isActive": true
                }
                """;

        String response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/api/v1/subscriptions/" + saved.getId())
                .then()
                .statusCode(200)
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("id").isEqualTo(saved.getId());
        assertThatJson(response).node("name").isEqualTo("Netflix Premium");
        assertThatJson(response).node("description").isEqualTo("Premium streaming service");
        assertThatJson(response).node("price").isEqualTo(19.99);
        assertThatJson(response).node("nextBillingDate").isEqualTo("2026-03-19");
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentSubscription() {
        String requestBody = """
                {
                    "name": "Netflix",
                    "description": "Streaming service",
                    "price": 15.99,
                    "billingCycle": "MONTHLY",
                    "nextBillingDate": "2026-02-19",
                    "isActive": true
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/api/v1/subscriptions/999")
                .then()
                .statusCode(404)
                .log().all()
                .body(containsString("Subscription not found with id: 999"));
    }

    @Test
    void shouldDeleteSubscription() {
        // Create test subscription
        Subscription subscription = Subscription.builder()
                .name("Netflix")
                .description("Streaming service")
                .price(new BigDecimal("15.99"))
                .billingCycle("MONTHLY")
                .nextBillingDate(LocalDate.of(2026, 2, 19))
                .isActive(true)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/v1/subscriptions/" + saved.getId())
                .then()
                .statusCode(204)
                .log().all();

        // Verify deletion
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/subscriptions/" + saved.getId())
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentSubscription() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/v1/subscriptions/999")
                .then()
                .statusCode(404)
                .log().all()
                .body(containsString("Subscription not found with id: 999"));
    }
}
