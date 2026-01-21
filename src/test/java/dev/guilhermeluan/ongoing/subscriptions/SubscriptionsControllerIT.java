package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.entitites.Subscriptions;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        Subscriptions subscription = createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1));


        var response = given().contentType(ContentType.JSON)
                .body(subscription)
                .when().post(API_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .log().all()
                .extract().body().asString();


        assertThatJson(response).node("id").isNotNull().isNumber();
        assertThatJson(response).node("name").isEqualTo(subscription.getName());
        assertThatJson(response).node("description").isEqualTo(subscription.getDescription());
        assertThatJson(response).node("value").isEqualTo(subscription.getValue());
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
                "BRL",
                null,
                null,
                null,
                null,
                null
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

    private List<Subscriptions> insertSampleSubscriptions() {
        Subscriptions subscriptions = subscriptionsRepository.save(createSubscription("Netflix", new BigDecimal("39.95"), LocalDate.now(), LocalDate.now().plusMonths(1)));
        Subscriptions subscriptions1 = subscriptionsRepository.save(createSubscription("Spotify", new BigDecimal("19.95"), LocalDate.now().minusDays(10), LocalDate.now().plusMonths(1)));

        return List.of(subscriptions, subscriptions1);
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