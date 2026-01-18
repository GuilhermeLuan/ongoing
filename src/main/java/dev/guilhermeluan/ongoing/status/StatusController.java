package dev.guilhermeluan.ongoing.status;

import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
        Status status = new Status();
        status.setTimestamp(new Timestamp(System.currentTimeMillis()));
        status.setMaxConnections(repository.getDatabaseMaxConnections());
        status.setOpenedConnections(repository.getOpenedConnections());
        status.setVersion(repository.getDatabaseVersion());
        return ResponseEntity.ok(status);
    }
}
