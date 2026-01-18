package dev.guilhermeluan.ongoing.flywayMigration;

import dev.guilhermeluan.ongoing.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class FlywayMigrationIT extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldHaveAppliedMigrations() {
        Integer migrationCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM flyway_schema_history", Integer.class
        );

//        assertThat(migrationCount)
//                .isGreaterThan(0);
    }
}