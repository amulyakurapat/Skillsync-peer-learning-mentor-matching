package com.skillsync.review.config;

import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerBaseUrlConfig {

    @Bean
    public ServerBaseUrlCustomizer reviewServerBaseUrlCustomizer() {
        // Force springdoc-generated base URL to the gateway origin.
        return (serverBaseUrl, request) -> "http://localhost:8080";
    }
}

