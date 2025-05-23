package com.maal.searchservice.infra.api;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class ExternalFlightApiConfig {

    @Value("${external.flight.api.currency}")
    private String currency;

    @Value("${external.flight.api.language}")
    private String language;

    @Value("${external.flight.api.key}")
    private String apiKey;

    @Value("${external.flight.api.engine}")
    private String defaultEngine;

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.query("currency", currency);
                template.query("hl", language);
                template.query("api_key", apiKey);
                template.query("engine", defaultEngine);
            }
        };
    }
}