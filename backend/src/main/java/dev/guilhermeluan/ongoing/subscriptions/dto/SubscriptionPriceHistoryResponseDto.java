package dev.guilhermeluan.ongoing.subscriptions.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionPriceHistoryResponseDto(
        Long id,
        Long subscriptionId,
        BigDecimal oldValue,
        BigDecimal newValue,
        BigDecimal changePercentage,
        Boolean isPriceSpike,
        LocalDateTime changedAt
) {
}
