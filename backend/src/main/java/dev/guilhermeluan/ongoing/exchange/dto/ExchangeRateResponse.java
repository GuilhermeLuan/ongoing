package dev.guilhermeluan.ongoing.exchange.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateResponse(
        String base,
        String date,
        Map<String, BigDecimal> rates
) {
}
