package dev.guilhermeluan.ongoing.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Total spending grouped by subscription category (values in BRL).
 */
public record CategorySpending(
        @NotNull Long categoryId,
        @NotBlank String categoryName,
        @NotNull @PositiveOrZero BigDecimal total
) {
}
