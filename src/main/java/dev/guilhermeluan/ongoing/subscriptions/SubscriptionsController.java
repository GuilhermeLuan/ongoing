package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionResponseDto;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionsController {

    private final SubscriptionsMapper subscriptionsMapper;

    private final SubscriptionsService subscriptionsService;

    @GetMapping
    public ResponseEntity<List<SubscriptionResponseDto>> findAll() {
        List<Subscriptions> subscriptions = subscriptionsService.findAll();

        List<SubscriptionResponseDto> response = subscriptionsMapper.toSubscriptionResponse(subscriptions);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    ResponseEntity<SubscriptionResponseDto> findById(@PathVariable long id) {
        Subscriptions subscription = subscriptionsService.findByIdOrThrowNotFoundException(id);

        SubscriptionResponseDto response = subscriptionsMapper.toSubscriptionResponse(subscription);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> create(@RequestBody @Valid SubscriptionRequestDto request) {

        Subscriptions subscriptionToSave = subscriptionsMapper.toSubscription(request);

        Subscriptions createdSubscription = subscriptionsService.save(subscriptionToSave);

        SubscriptionResponseDto response = subscriptionsMapper.toSubscriptionResponse(createdSubscription);

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> update(@PathVariable long id, @RequestBody @Valid SubscriptionRequestDto request) {
        Subscriptions subscriptionToUpdate = subscriptionsMapper.toSubscription(request);

        Subscriptions updatedSubscription = subscriptionsService.update(id, subscriptionToUpdate);

        SubscriptionResponseDto response = subscriptionsMapper.toSubscriptionResponse(updatedSubscription);

        return ResponseEntity.ok(response);
    }

}
