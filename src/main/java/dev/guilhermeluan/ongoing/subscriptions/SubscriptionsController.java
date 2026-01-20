package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionResponseDto;
import dev.guilhermeluan.ongoing.subscriptions.entitites.Subscriptions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionsController {

    private final SubscriptionsMapper subscriptionsMapper;

    private final SubscriptionsService subscriptionsService;

    @GetMapping
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions() {
        List<Subscriptions> subscriptions = subscriptionsService.findAll();

        List<SubscriptionResponseDto> response = subscriptionsMapper.toSubscriptionResponse(subscriptions);

        return ResponseEntity.ok(response);
    }

}
