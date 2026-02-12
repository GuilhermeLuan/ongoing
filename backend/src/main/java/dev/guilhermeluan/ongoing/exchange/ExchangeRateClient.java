package dev.guilhermeluan.ongoing.exchange;

import dev.guilhermeluan.ongoing.exchange.dto.ExchangeRateResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface ExchangeRateClient {

    @GetExchange
    ExchangeRateResponse fetchRates();
}
