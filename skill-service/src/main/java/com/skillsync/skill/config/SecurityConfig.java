package com.skillsync.skill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.skillsync.skill.filter.JwtRoleFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRoleFilter jwtRoleFilter;

    public SecurityConfig(JwtRoleFilter jwtRoleFilter) {
        this.jwtRoleFilter = jwtRoleFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/skills", "/skills/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/skills").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/skills/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/skills/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtRoleFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}