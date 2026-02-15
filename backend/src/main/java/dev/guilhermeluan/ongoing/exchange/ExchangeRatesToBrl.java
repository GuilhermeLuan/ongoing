package dev.guilhermeluan.ongoing.exchange;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRatesToBrl(
        BigDecimal usdToBrl,
        BigDecimal eurToBrl,
        LocalDate exchangeRateDate
) {
}
