package com.skillsync.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springdoc.core.customizers.ServerBaseUrlCustomizer;

@Configuration
public class OpenApiServersConfig {

    @Bean
    public ServerBaseUrlCustomizer usersServerBaseUrlCustomizer() {
        // springdoc will otherwise generate "Generated server url" from server.port (8082).
        // This forces the base URL to the gateway origin (same-origin => no CORS).
        return (serverBaseUrl, request) -> "http://localhost:8080";
    }
}

