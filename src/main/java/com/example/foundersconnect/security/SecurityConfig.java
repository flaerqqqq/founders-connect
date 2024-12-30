package com.example.foundersconnect.security;

import com.example.foundersconnect.filters.HttpLogFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(request ->
                        request
                                .anyRequest().permitAll()
                )
                .addFilterBefore(new HttpLogFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}