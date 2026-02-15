package dev.guilhermeluan.ongoing.exchange;

import dev.guilhermeluan.ongoing.exchange.dto.ExchangeRateResponse;
import dev.guilhermeluan.ongoing.subscriptions.entities.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateClient client;

    @InjectMocks
    private ExchangeRateService service;

    @Test
    void convertToBrl_ShouldNotCallApi_WhenCurrencyIsBrl() {
        BigDecimal result = service.convertToBrl(new BigDecimal("10.00"), Currency.BRL);

        assertThat(result).isEqualByComparingTo("10.00");
        verify(client, never()).fetchRates();
    }

    @Test
    void convertToBrl_ShouldConvertUsdToBrl_WhenBaseIsUsd() {
        when(client.fetchRates()).thenReturn(new ExchangeRateResponse(
                "USD",
                "2026-02-03",
                Map.of(
                        "BRL", new BigDecimal("5.00"),
                        "EUR", new BigDecimal("0.80")
                )
        ));

        BigDecimal result = service.convertToBrl(new BigDecimal("10.00"), Currency.USD);

        assertThat(result).isEqualByComparingTo("50.00");
    }

    @Test
    void convertToBrl_ShouldConvertEurToBrl_WhenBaseIsUsd() {
        when(client.fetchRates()).thenReturn(new ExchangeRateResponse(
                "USD",
                "2026-02-03",
                Map.of(
                        "BRL", new BigDecimal("5.00"),
                        "EUR", new BigDecimal("0.80")
                )
        ));

        BigDecimal result = service.convertToBrl(new BigDecimal("10.00"), Currency.EUR);

        assertThat(result).isEqualByComparingTo("62.50");
    }

    @Test
    void getRatesToBrl_ShouldThrow_WhenMissingRequiredRates() {
        when(client.fetchRates()).thenReturn(new ExchangeRateResponse(
                "USD",
                "2026-02-03",
                Map.of("EUR", new BigDecimal("0.80"))
        ));

        assertThatThrownBy(() -> service.getRatesToBrl())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BRL");
    }
}
