package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.subscriptions.entitites.Subscriptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionsService {
    private final SubscriptionsRepository subscriptionsRepository;

    public SubscriptionsService(SubscriptionsRepository subscriptionsRepository) {this.subscriptionsRepository = subscriptionsRepository;}

    public Subscriptions save(Subscriptions subscription) {
        return subscriptionsRepository.save(subscription);
    }

    public List<Subscriptions> findAll() {
        return subscriptionsRepository.findAll();
    }

    public Subscriptions findByIdOrThrowNotFoundException(Long id) {
        return subscriptionsRepository.findById(id).orElseThrow(() -> new SubscriptionNotFoundException(id));
    }

    public void deleteById(Long id) {
        findByIdOrThrowNotFoundException(id);
        subscriptionsRepository.deleteById(id);
    }

    public Subscriptions update(Long id, Subscriptions subscriptionToUpdate) {
        Subscriptions existingSubscription = findByIdOrThrowNotFoundException(id);
        subscriptionToUpdate.setId(existingSubscription.getId());
        return subscriptionsRepository.save(subscriptionToUpdate);
    }

}
