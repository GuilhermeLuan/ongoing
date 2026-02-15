package dev.guilhermeluan.ongoing.exchange;

import dev.guilhermeluan.ongoing.exchange.dto.ExchangeRateResponse;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateClient client;

    private static BigDecimal computeRateToBrl(ExchangeRateResponse response, Currency base, Currency from) {
        if (from == Currency.BRL) {
            return BigDecimal.ONE;
        }

        BigDecimal baseToBrl = base == Currency.BRL
                ? BigDecimal.ONE
                : getRate(response, Currency.BRL);

        if (from == base) {
            return baseToBrl;
        }

        BigDecimal baseToFrom = getRate(response, from);
        return baseToBrl.divide(baseToFrom, 8, RoundingMode.HALF_UP);
    }

    private static BigDecimal getRate(ExchangeRateResponse response, Currency currency) {
        BigDecimal rate = response.rates().get(currency.name());
        if (rate == null) {
            throw new IllegalStateException("Exchange rate API response does not contain rate for " + currency);
        }
        return rate;
    }

    private static Currency parseCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalStateException("Exchange rate API response does not contain base currency");
        }
        try {
            return Currency.valueOf(currency);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported base currency from exchange API: " + currency);
        }
    }

    private static LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(date);
        } catch (Exception ignored) {
            return LocalDate.now();
        }
    }

    @Cacheable(value = "exchange-rates", key = "'BRL'")
    public ExchangeRatesToBrl getRatesToBrl() {
        ExchangeRateResponse response = client.fetchRates();

        if (response == null) {
            throw new IllegalStateException("Exchange rate API returned null response");
        }
        if (response.rates() == null) {
            throw new IllegalStateException("Exchange rate API response does not contain rates");
        }

        Currency base = parseCurrency(response.base());
        LocalDate date = parseDate(response.date());

        BigDecimal usdToBrl = computeRateToBrl(response, base, Currency.USD);
        BigDecimal eurToBrl = computeRateToBrl(response, base, Currency.EUR);

        return new ExchangeRatesToBrl(usdToBrl, eurToBrl, date);
    }

    public BigDecimal convertToBrl(BigDecimal amount, Currency from) {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (from == null) {
            throw new IllegalArgumentException("from must not be null");
        }

        if (from == Currency.BRL) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }

        ExchangeRatesToBrl rates = getRatesToBrl();
        BigDecimal rate = switch (from) {
            case USD -> rates.usdToBrl();
            case EUR -> rates.eurToBrl();
            case BRL -> BigDecimal.ONE;
        };

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
