package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.exception.BadRequestException;
import dev.guilhermeluan.ongoing.exception.NotFoundException;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SubscriptionsService {
    private final SubscriptionsRepository subscriptionsRepository;

    public SubscriptionsService(SubscriptionsRepository subscriptionsRepository) {this.subscriptionsRepository = subscriptionsRepository;}

    public Subscriptions save(Subscriptions subscription) {

        LocalDate nextBillingDate = calculateNextBillingDate(subscription);
        subscription.setNextPaymentDate(nextBillingDate);

        return subscriptionsRepository.save(subscription);
    }

    public Page<Subscriptions> findAll(Pageable pageable) {
        return subscriptionsRepository.findAll(pageable);
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
        Long billingCycleId = subscription.getBillingCycle().getId();

        if (billingCycleId == 1L) {
            return lastBillingDate.plusMonths(1);
        } else if (billingCycleId == 2L) {
            return lastBillingDate.plusYears(1);
        }
        throw new BadRequestException(HttpStatus.BAD_REQUEST, "Unknown billing cycle: " + billingCycleId);
    }
}
