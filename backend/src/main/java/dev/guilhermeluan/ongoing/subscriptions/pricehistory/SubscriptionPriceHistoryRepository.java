package dev.guilhermeluan.ongoing.subscriptions.pricehistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionPriceHistoryRepository extends JpaRepository<SubscriptionPriceHistory, Long> {

    List<SubscriptionPriceHistory> findBySubscription_IdAndUser_IdOrderByChangedAtDesc(Long subscriptionId, Long userId);

    List<SubscriptionPriceHistory> findByUser_IdAndIsPriceSpikeTrueAndChangedAtBetweenOrderByChangedAtDesc(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
    );
}
