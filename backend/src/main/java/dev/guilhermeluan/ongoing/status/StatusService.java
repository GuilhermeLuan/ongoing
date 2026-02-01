package dev.guilhermeluan.ongoing.status;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class StatusService {

    private final StatusRepository repository;

    public StatusService(StatusRepository statusRepository) {
        this.repository = statusRepository;
    }

    public StatusResponse getStatus() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Integer maxConnections = repository.getMaxConnections();
        Integer openedConnections = repository.getOpenedConnections();
        String version = repository.getVersion();

        StatusResponse.DatabaseInfo databaseInfo = StatusResponse.DatabaseInfo.builder()
                .maxConnections(maxConnections)
                .openedConnections(openedConnections)
                .version(version)
                .build();
        StatusResponse.Dependencies dependencies = StatusResponse.Dependencies.builder()
                .database(databaseInfo)
                .build();

        return StatusResponse.builder()
                .updatedAt(timestamp)
                .dependencies(dependencies)
                .build();
    }
}
