package com.storefit.orders_service.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${users-service.url}")
    private String usersBaseUrl;

    @Value("${catalog-service.url}")
    private String catalogBaseUrl;

    @Bean
    public WebClient usersWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(usersBaseUrl)
                .build();
    }

    @Bean
    public WebClient catalogWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(catalogBaseUrl)
                .build();
    }
}
