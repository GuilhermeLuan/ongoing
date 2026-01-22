package dev.guilhermeluan.ongoing.subscriptions.dto;

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

        @NotNull(message = "Next payment date is required")
        LocalDate nextPaymentDate,

        Boolean active,

        Boolean notifyUser,

        @Size(max = 3, message = "Currency must be at most 3 characters")
        String currency,

        @Size(max = 255, message = "Logo URL must be at most 255 characters")
        String logoUrl,

        Long categoryId,
        Long paymentMethodId,
        Long billingCycleId,
        Long subscriptionTypeId
) {
}
