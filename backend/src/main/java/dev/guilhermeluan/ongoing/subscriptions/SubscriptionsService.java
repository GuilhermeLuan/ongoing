package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.exception.BadRequestException;
import dev.guilhermeluan.ongoing.exception.NotFoundException;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.user.User;
import dev.guilhermeluan.ongoing.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SubscriptionsService {
    private final SubscriptionsRepository subscriptionsRepository;
    private final UserRepository userRepository;

    public SubscriptionsService(SubscriptionsRepository subscriptionsRepository, UserRepository userRepository) {
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

    public Subscriptions update(Long id, Subscriptions subscription, Long userId) {
        findByIdOrThrowNotFoundException(id, userId);

        LocalDate nextBillingDate = calculateNextBillingDate(subscription);
        subscription.setNextPaymentDate(nextBillingDate);

        return subscriptionsRepository.save(subscription);
    }

    public List<Subscriptions> findActiveByUserId(Long userId) {
        return subscriptionsRepository.findActiveByUserId(userId);
    }

    public LocalDate calculateNextBillingDate(Subscriptions subscription) {
        LocalDate lastBillingDate = subscription.getStartDate();
        BillingCycle billingCycle = subscription.getBillingCycle();

        if (billingCycle == null) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Billing cycle is required");
        }

        return switch (billingCycle) {
            case MONTHLY -> lastBillingDate.plusMonths(1);
            case QUARTERLY -> lastBillingDate.plusMonths(3);
            case SEMI_ANNUAL -> lastBillingDate.plusMonths(6);
            case YEARLY -> lastBillingDate.plusYears(1);
            case WEEKLY -> lastBillingDate.plusWeeks(1);
            case BIWEEKLY -> lastBillingDate.plusWeeks(2);
        };
    }

    public Map<User, List<Subscriptions>> findRenewalSubscriptionsGroupedByUser(LocalDate date) {
        List<Subscriptions> subscriptions = subscriptionsRepository.findByNextPaymentDateAndActiveAndNotify(date);
        return subscriptions.stream().collect(Collectors.groupingBy(Subscriptions::getUser));
    }
}
