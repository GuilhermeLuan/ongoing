package dev.guilhermeluan.ongoing.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public interface TestcontainersConfigurations {
    @Container
    @ServiceConnection
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16.0-alpine3.18");
}
