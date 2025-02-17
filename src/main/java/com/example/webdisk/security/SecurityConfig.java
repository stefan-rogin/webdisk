package com.example.webdisk.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * Security configuration class for the web application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Creates and configures a {@link BearerAuthenticationFilter} bean.
     * 
     * @return a new instance of {@link BearerAuthenticationFilter}
     */
    @Bean
    public BearerAuthenticationFilter bearerAuthenticationFilter() {
        return new BearerAuthenticationFilter();
    }

    /**
     * Configures the security filter chain for the application.
     * 
     * <p>This method sets up the security configuration using {@link HttpSecurity}.
     * It disables CSRF protection, requires authentication for requests to 
     * "/files/restricted", and permits all other requests. Additionally, it adds 
     * a custom bearer authentication filter before the 
     * {@link AbstractPreAuthenticatedProcessingFilter}.</p>
     * 
     * @param http the {@link HttpSecurity} to modify
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while configuring the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/files/restricted").authenticated()
                .anyRequest().permitAll())
                .addFilterBefore(bearerAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter.class);
        return http.build();
    }

}
