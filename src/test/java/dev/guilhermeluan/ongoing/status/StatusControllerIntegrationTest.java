package dev.guilhermeluan.ongoing.status;

import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class StatusControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnStatusInfo() {
        String response = given().contentType(ContentType.JSON)
                .when().get("/api/v1/status")
                .then()
                .statusCode(200)
                .log().all()
                .extract().body().asString();

        assertThatJson(response).node("updatedAt").isNotNull();
        assertThatJson(response).node("dependencies.database.maxConnections").isNotNull().isEqualTo(100);
        assertThatJson(response).node("dependencies.database.version").isNotNull().isString().isEqualTo("16.0");
        assertThatJson(response).node("dependencies.database.openedConnections").isNotNull().isNumber();

    }

}