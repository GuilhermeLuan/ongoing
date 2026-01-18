package dev.guilhermeluan.ongoing.status;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class Status {
    private Timestamp timestamp;
    private Integer maxConnections;
    private Integer openedConnections;
    private String version;
}
