package dev.guilhermeluan.ongoing.dashboard;

import dev.guilhermeluan.ongoing.auth.jwt.UserPrincipal;
import dev.guilhermeluan.ongoing.dashboard.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            Authentication auth
    ) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).id();
        log.info("Fetching dashboard for user {}", userId);

        DashboardResponse response = dashboardService.getDashboard(userId);
        return ResponseEntity.ok(response);
    }
}
