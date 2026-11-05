package com.rozplochowski.loyaltypoints.config;

import com.rozplochowski.loyaltypoints.service.orders.NicknameClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class HttpClientConfiguration {

    @Bean
    NicknameClient nicknameClient() {
        var client = RestClient.create("https://randomuser.me");

        var proxyFactory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(client))
                .build();

        return proxyFactory.createClient(NicknameClient.class);
    }
}
