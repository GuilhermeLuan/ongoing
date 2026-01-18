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
    public ResponseEntity<Status> getStatus() {

        Status status = Status.builder()
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .maxConnections(repository.getDatabaseMaxConnections())
                .openedConnections(repository.getOpenedConnections())
                .version(repository.getDatabaseVersion())
                .build();

        return ResponseEntity.ok(status);
    }
}
