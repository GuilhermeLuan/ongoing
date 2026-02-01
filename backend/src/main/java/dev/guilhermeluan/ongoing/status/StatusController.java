package dev.guilhermeluan.ongoing.status;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    private final StatusService service;

    public StatusController(StatusService statusService) {
        this.service = statusService;
    }

    @GetMapping
    public ResponseEntity<StatusResponse> getStatus() {
        StatusResponse status = service.getStatus();
        return ResponseEntity.ok(status);
    }
}
