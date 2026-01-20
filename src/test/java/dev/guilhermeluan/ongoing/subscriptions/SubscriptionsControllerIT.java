package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import dev.guilhermeluan.ongoing.subscriptions.entitites.Subscriptions;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class SubscriptionsControllerIT extends BaseIntegrationTest {


    public static final String API_URL = "/api/v1/subscriptions";

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;

    @BeforeEach
    void cleanDatabase() {
        subscriptionsRepository.deleteAll();
    }

    @Test
    void shouldReturnAllSubscriptions() {
        insertSampleSubscriptions();

        String response = given().contentType(ContentType.JSON)
                .when().get(API_URL)
                .then()
                .statusCode(200)
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

    private void insertSampleSubscriptions() {
        subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1)));
        subscriptionsRepository.save(createSubscription("Spotify", new BigDecimal("19.95"), LocalDate.now().minusDays(10), LocalDate.now().plusMonths(1)));
    }

    private Subscriptions createSubscription(String name, BigDecimal value, LocalDate startDate, LocalDate nextPaymentDate) {
        return Subscriptions.builder()
                .name(name)
                .description(name + " mensal")
                .value(value)
                .startDate(startDate)
                .nextPaymentDate(nextPaymentDate)
                .currency("BRL")
                .notify(true)
                .active(true)
                .build();
    }
}