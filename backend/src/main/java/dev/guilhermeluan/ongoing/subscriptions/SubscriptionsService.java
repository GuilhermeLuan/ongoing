package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.exception.BadRequestException;
import dev.guilhermeluan.ongoing.exception.NotFoundException;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import dev.guilhermeluan.ongoing.subscriptions.pricehistory.SubscriptionPriceHistory;
import dev.guilhermeluan.ongoing.subscriptions.pricehistory.SubscriptionPriceHistoryRepository;
import dev.guilhermeluan.ongoing.user.User;
import dev.guilhermeluan.ongoing.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubscriptionsService {
    private final SubscriptionsRepository subscriptionsRepository;
    private final UserRepository userRepository;
    private final SubscriptionPriceHistoryRepository subscriptionPriceHistoryRepository;
    private final SubscriptionsMapper subscriptionsMapper;

    public SubscriptionsService(SubscriptionsRepository subscriptionsRepository, UserRepository userRepository, SubscriptionPriceHistoryRepository subscriptionPriceHistoryRepository, SubscriptionsMapper subscriptionsMapper) {
        this.subscriptionsRepository = subscriptionsRepository;
        this.userRepository = userRepository;
        this.subscriptionPriceHistoryRepository = subscriptionPriceHistoryRepository;
        this.subscriptionsMapper = subscriptionsMapper;
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

    public Subscriptions update(Long id, SubscriptionRequestDto request, Long userId) {
        Subscriptions subscription = findByIdOrThrowNotFoundException(id, userId);
        BigDecimal oldValue = subscription.getValue();

        subscriptionsMapper.updateSubscriptionFromDto(request, subscription);
        BigDecimal newValue = subscription.getValue();

        return persistUpdatedSubscription(id, subscription, userId, oldValue, newValue);
    }

    public Subscriptions update(Long id, Subscriptions subscription, Long userId) {
        BigDecimal oldValue = findByIdOrThrowNotFoundException(id, userId).getValue();
        BigDecimal newValue = subscription.getValue();
        return persistUpdatedSubscription(id, subscription, userId, oldValue, newValue);
    }

    private Subscriptions persistUpdatedSubscription(Long id, Subscriptions subscription, Long userId, BigDecimal oldValue, BigDecimal newValue) {
        log.info("Updating subscription {} for user {}", id, userId);
        log.debug("Incoming subscription update payload: id={}, name={}, value={}, startDate={}, billingCycle={}, active={}, notify={}",
                subscription.getId(),
                subscription.getName(),
                subscription.getValue(),
                subscription.getStartDate(),
                subscription.getBillingCycle(),
                subscription.getActive(),
                subscription.getNotify());

        log.debug("Current subscription state: id={}, name={}, value={}, nextPaymentDate={}, billingCycle={}, active={}, notify={}",
                subscription.getId(),
                subscription.getName(),
                oldValue,
                subscription.getNextPaymentDate(),
                subscription.getBillingCycle(),
                subscription.getActive(),
                subscription.getNotify());

        LocalDate nextBillingDate = calculateNextBillingDate(subscription);
        subscription.setNextPaymentDate(nextBillingDate);

        log.debug("Calculated next billing date for subscription {}: {}", id, nextBillingDate);

        if (oldValue.compareTo(newValue) != 0) {
            log.info("Subscription {} price changed for user {}: oldValue={}, newValue={}", id, userId, oldValue, newValue);
            BigDecimal changePercentage = BigDecimal.ZERO;
            boolean isSpike = false;

            if(oldValue.compareTo(BigDecimal.ZERO) > 0){
                changePercentage = newValue.subtract(oldValue)
                        .divide(oldValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

                isSpike = changePercentage.compareTo(BigDecimal.valueOf(10)) >= 0;
            }

            log.debug("Price change details for subscription {}: changePercentage={}%, isSpike={}", id, changePercentage, isSpike);

            SubscriptionPriceHistory priceHistory = SubscriptionPriceHistory.builder()
                    .newValue(newValue)
                    .oldValue(oldValue)
                    .changePercentage(changePercentage)
                    .isPriceSpike(isSpike)
                    .user(userRepository.getReferenceById(userId))
                    .subscription(subscription)
                    .build();


            subscriptionPriceHistoryRepository.save(priceHistory);
            log.info("Price history saved for subscription {} and user {}", id, userId);
        } else {
            log.debug("Subscription {} price unchanged for user {}", id, userId);
        }

        Subscriptions updatedSubscription = subscriptionsRepository.save(subscription);
        log.info("Subscription {} updated for user {}", updatedSubscription.getId(), userId);

        return updatedSubscription;
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
