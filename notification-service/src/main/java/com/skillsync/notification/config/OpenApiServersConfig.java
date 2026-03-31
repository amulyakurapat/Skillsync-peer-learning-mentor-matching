package com.skillsync.notification.config;

import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiServersConfig {

    @Bean
    public ServerBaseUrlCustomizer notificationsServerBaseUrlCustomizer() {
        // Force springdoc-generated server base URL to the gateway origin.
        return (serverBaseUrl, request) -> "http://localhost:8080";
    }
}

