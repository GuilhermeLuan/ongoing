package dev.guilhermeluan.ongoing.status;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;

@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    private final StatusRepository repository;

    public StatusController(StatusRepository statusRepository) {
        this.repository = statusRepository;
    }

    @GetMapping
    public ResponseEntity<StatusResponse> getStatus() {
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

        StatusResponse status = StatusResponse.builder()
                .updatedAt(timestamp)
                .dependencies(dependencies)
                .build();

        return ResponseEntity.ok(status);
    }
}
