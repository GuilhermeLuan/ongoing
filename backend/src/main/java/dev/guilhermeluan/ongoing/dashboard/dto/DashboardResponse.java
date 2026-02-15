package dev.guilhermeluan.ongoing.dashboard.dto;

import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consolidated dashboard statistics for an authenticated user.
 * All monetary values are returned in BRL.
 */
public record DashboardResponse(
        @NotNull List<CategorySpending> spendingByCategory,
        @NotNull @PositiveOrZero BigDecimal monthlyAverage,
        @NotNull @PositiveOrZero BigDecimal thisMonthTotal,
        @NotNull @PositiveOrZero BigDecimal yearlyTotal,
        @NotNull Currency currency,
        @NotNull LocalDate exchangeRateDate
) {
}
