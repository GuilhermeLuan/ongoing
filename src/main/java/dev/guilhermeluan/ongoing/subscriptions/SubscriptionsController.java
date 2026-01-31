package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.auth.jwt.UserPrincipal;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionResponseDto;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionsController {

    private final SubscriptionsMapper subscriptionsMapper;

    private final SubscriptionsService subscriptionsService;

    @GetMapping
    public ResponseEntity<Page<SubscriptionResponseDto>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long categoryId,
            Authentication auth,
            @PageableDefault Pageable pageable) {

        Long userId = ((UserPrincipal) auth.getPrincipal()).id();

        Page<SubscriptionResponseDto> response = subscriptionsService.findAll(
                name,
                active,
                categoryId,
                pageable,
                userId
        ).map(subscriptionsMapper::toSubscriptionResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    ResponseEntity<SubscriptionResponseDto> findById(@PathVariable long id, Authentication auth) {

        Long userId = ((UserPrincipal) auth.getPrincipal()).id();
        Subscriptions subscription = subscriptionsService.findByIdOrThrowNotFoundException(id, userId);

        SubscriptionResponseDto response = subscriptionsMapper.toSubscriptionResponse(subscription);

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> create(@RequestBody @Valid SubscriptionRequestDto request, Authentication auth) {

        Long userId = ((UserPrincipal) auth.getPrincipal()).id();

        Subscriptions subscriptionToSave = subscriptionsMapper.toSubscription(request);

        Subscriptions createdSubscription = subscriptionsService.save(subscriptionToSave, userId);

        SubscriptionResponseDto response = subscriptionsMapper.toSubscriptionResponse(createdSubscription);

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> update(@PathVariable long id, @RequestBody @Valid SubscriptionRequestDto request, Authentication auth) {

        Long userId = ((UserPrincipal) auth.getPrincipal()).id();
        Subscriptions subscription = subscriptionsService.findByIdOrThrowNotFoundException(id, userId);

        subscriptionsMapper.updateSubscriptionFromDto(request, subscription);

        Subscriptions updatedSubscription = subscriptionsService.update(id, subscription, userId);

        SubscriptionResponseDto response = subscriptionsMapper.toSubscriptionResponse(updatedSubscription);

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<Void> delete(@PathVariable long id, Authentication auth) {

        Long userId = ((UserPrincipal) auth.getPrincipal()).id();
        subscriptionsService.deleteById(id, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
