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

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @GetMapping("/{id}")
    ResponseEntity<SubscriptionResponseDto> findById(@PathVariable long id) {
        Subscriptions subscription = subscriptionsService.findByIdOrThrowNotFoundException(id);

        SubscriptionResponseDto response = subscriptionsMapper.toSubscriptionResponse(subscription);

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
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
        Subscriptions subscription = subscriptionsService.findByIdOrThrowNotFoundException(id);

        subscriptionsMapper.updateSubscriptionFromDto(request, subscription);

        Subscriptions updatedSubscription = subscriptionsService.update(id, subscription);

        SubscriptionResponseDto response = subscriptionsMapper.toSubscriptionResponse(updatedSubscription);

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<Void> delete(@PathVariable long id) {
        subscriptionsService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
