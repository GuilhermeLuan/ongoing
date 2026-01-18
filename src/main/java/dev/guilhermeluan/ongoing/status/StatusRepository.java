package dev.guilhermeluan.ongoing.status;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class StatusRepository {
    private final JdbcTemplate jdbcTemplate;

    public StatusRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getVersion() {
        String sql = "SHOW server_version;";
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    public Integer getMaxConnections() {
        String sql = "SHOW max_connections;";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public Integer getOpenedConnections() {
        String sql = "SELECT count(*)::int FROM pg_stat_activity WHERE datname = current_database()";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}