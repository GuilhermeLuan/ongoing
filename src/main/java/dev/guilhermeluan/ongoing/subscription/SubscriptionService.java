package dev.guilhermeluan.ongoing.subscription;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repository;

    public SubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> findAll() {
        return repository.findAll().stream()
                .map(SubscriptionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse findById(Long id) {
        Subscription subscription = repository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found with id: " + id));
        return SubscriptionResponse.fromEntity(subscription);
    }

    @Transactional
    public SubscriptionResponse create(SubscriptionRequest request) {
        Subscription subscription = Subscription.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .billingCycle(request.getBillingCycle())
                .nextBillingDate(request.getNextBillingDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Subscription savedSubscription = repository.save(subscription);
        return SubscriptionResponse.fromEntity(savedSubscription);
    }

    @Transactional
    public SubscriptionResponse update(Long id, SubscriptionRequest request) {
        Subscription subscription = repository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found with id: " + id));

        subscription.setName(request.getName());
        subscription.setDescription(request.getDescription());
        subscription.setPrice(request.getPrice());
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setNextBillingDate(request.getNextBillingDate());
        if (request.getIsActive() != null) {
            subscription.setIsActive(request.getIsActive());
        }

        Subscription updatedSubscription = repository.save(subscription);
        return SubscriptionResponse.fromEntity(updatedSubscription);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new SubscriptionNotFoundException("Subscription not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
