package dev.guilhermeluan.ongoing.status;

import java.sql.Timestamp;

public class Status {
    private Timestamp timestamp;
    private Integer maxConnections;
    private Integer openedConnections;
    private String version;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Integer getOpenedConnections() {
        return openedConnections;
    }

    public void setOpenedConnections(Integer openedConnections) {
        this.openedConnections = openedConnections;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
