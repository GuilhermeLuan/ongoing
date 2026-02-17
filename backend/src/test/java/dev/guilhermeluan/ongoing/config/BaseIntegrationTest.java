package dev.guilhermeluan.ongoing.config;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportTestcontainers(TestcontainersConfigurations.class)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthRateLimitInterceptor authRateLimitInterceptor;

    @Autowired
    private ApiRateLimitInterceptor apiRateLimitInterceptor;

    @BeforeEach()
    protected void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        authRateLimitInterceptor.resetBuckets();
        apiRateLimitInterceptor.resetBuckets();
    }
}
