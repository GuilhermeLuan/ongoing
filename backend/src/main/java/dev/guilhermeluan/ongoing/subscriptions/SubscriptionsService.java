package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.exception.BadRequestException;
import dev.guilhermeluan.ongoing.exception.NotFoundException;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.User;
import dev.guilhermeluan.ongoing.user.UserRepository;
import dev.guilhermeluan.ongoing.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SubscriptionsService {
    private final SubscriptionsRepository subscriptionsRepository;
    private final UserRepository userRepository;

    public SubscriptionsService(SubscriptionsRepository subscriptionsRepository, UserService userService, UserRepository userRepository) {
        this.subscriptionsRepository = subscriptionsRepository;
        this.userRepository = userRepository;
    }

    public Subscriptions save(Subscriptions subscription, Long userId) {
        User user = userRepository.getReferenceById(userId);
        subscription.setUser(user);
        LocalDate nextBillingDate = calculateNextBillingDate(subscription);
        subscription.setNextPaymentDate(nextBillingDate);

        return subscriptionsRepository.save(subscription);
    }

    public Page<Subscriptions> findAll(
            String name,
            Boolean active,
            Long categoryId,
            Pageable pageable,
            Long userId
    ) {

        if (name != null || active != null || categoryId != null) {
            return subscriptionsRepository.findWithFilters(name, active, categoryId, userId, pageable);
        }

        return subscriptionsRepository.findAllByUserId(userId, pageable);
    }

    public Subscriptions findByIdOrThrowNotFoundException(Long id, Long userId) {
        return subscriptionsRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND, "Subscription not found"));
    }

    public void deleteById(Long id, Long userId) {
        findByIdOrThrowNotFoundException(id, userId);
        subscriptionsRepository.deleteById(id);
    }

    public Subscriptions update(Long id, Subscriptions subscriptionToUpdate, Long userId) {
        Subscriptions existingSubscription = findByIdOrThrowNotFoundException(id, userId);
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
