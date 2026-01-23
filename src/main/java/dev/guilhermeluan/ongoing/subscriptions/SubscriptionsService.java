package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.exception.NotFoundException;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionsService {
    private final SubscriptionsRepository subscriptionsRepository;

    public SubscriptionsService(SubscriptionsRepository subscriptionsRepository) {this.subscriptionsRepository = subscriptionsRepository;}

    public Subscriptions save(Subscriptions subscription) {

        LocalDate nextBillingDate = calculateNextBillingDate(subscription);
        subscription.setNextPaymentDate(nextBillingDate);

        return subscriptionsRepository.save(subscription);
    }

    public List<Subscriptions> findAll() {
        return subscriptionsRepository.findAll();
    }

    public Subscriptions findByIdOrThrowNotFoundException(Long id) {
        return subscriptionsRepository.findById(id).orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND, "Subscription not found"));
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

    public LocalDate calculateNextBillingDate(Subscriptions subscription) {
        LocalDate lastBillingDate = subscription.getStartDate();
        BillingCycle billingCycle = subscription.getBillingCycle();


        if (billingCycle == null || billingCycle.getId() == null) {
            throw new IllegalArgumentException("Billing cycle is required");
        }


        if (billingCycle.getId() == 1) {
            return lastBillingDate.plusMonths(1);
        } else if (billingCycle.getId() == 2) {
            return lastBillingDate.plusYears(1);
        }
        throw new IllegalArgumentException("Unknown billing cycle: " + subscription.getBillingCycle());
    }

}
