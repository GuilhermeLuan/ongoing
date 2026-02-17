package dev.guilhermeluan.ongoing.subscriptions.dto;

import dev.guilhermeluan.ongoing.subscriptions.entities.BillingCycle;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionRequestDto(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be at most 255 characters")
        String name,

        @Size(max = 255, message = "Description must be at most 255 characters")
        String description,

        @NotNull(message = "Value is required")
        @Positive(message = "Value must be positive")
        BigDecimal value,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate nextPaymentDate,

        Boolean active,

        Boolean notifyUser,

        Currency currency,

        @Size(max = 255, message = "Logo URL must be at most 255 characters")
        String logoUrl,

        Long categoryId,
        Long paymentMethodId,
        @NotNull(message = "BillingCycle is required")
        BillingCycle billingCycle,
        Long subscriptionTypeId
) {
}
