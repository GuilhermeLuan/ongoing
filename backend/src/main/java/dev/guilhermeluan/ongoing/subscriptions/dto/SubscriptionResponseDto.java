package dev.guilhermeluan.ongoing.subscriptions.dto;

import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;

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
        Boolean notifyUser,
        Currency currency,
        String logoUrl,
        Long categoryId,
        Long paymentMethodId,
        BillingCycle billingCycle,
        Long subscriptionTypeId,
        String categoryName,
        String paymentMethodName
) {
}
