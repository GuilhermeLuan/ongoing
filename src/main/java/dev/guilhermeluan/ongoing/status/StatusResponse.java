package dev.guilhermeluan.ongoing.status;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class StatusResponse {
    private Timestamp updatedAt;

    private Dependencies dependencies;

    @Data
    @Builder
    public static class Dependencies {
        private DatabaseInfo database;
    }

    @Data
    @Builder
    public static class DatabaseInfo {
        private Integer maxConnections;
        private Integer openedConnections;
        private String version;
    }
}