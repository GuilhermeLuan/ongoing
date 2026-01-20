package dev.guilhermeluan.ongoing.subscriptions.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal value,
        LocalDate startDate,
        LocalDate nextPaymentDate,
        Boolean active,
        Boolean isNotifyEnabled,
        String currency,
        String logoUrl,
        Long categoryId,
        Long paymentMethodId,
        Long billingCycleId,
        Long subscriptionTypeId
) {
}
