package com.skillsync.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SkillSync API Gateway")
                        .description("Unified API documentation for all SkillSync services")
                        .version("1.0"));
    }
}