package dev.guilhermeluan.ongoing.exchange;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ExchangeRateClientConfig {

    @Bean
    public ExchangeRateClient exchangeRateClient(
            @Value("${exchange.api.url}") String exchangeApiUrl
    ) {
        RestClient restClient = RestClient.builder()
                .baseUrl(exchangeApiUrl)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(ExchangeRateClient.class);
    }
}
