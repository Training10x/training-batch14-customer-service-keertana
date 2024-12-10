package com.example.customer_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String  API_PATTERN = "/api/v1/customers/**";
    // Password encoder for secure password storage
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Security filter chain configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Disable CSRF for simplicity (enable it in production)
                .authorizeHttpRequests()
                // Allow GET requests for all customers
                .requestMatchers(HttpMethod.GET, API_PATTERN).permitAll()
                // Restrict POST requests to ADMIN role
                .requestMatchers(HttpMethod.POST, API_PATTERN).hasRole(ROLE_ADMIN)
                // Restrict PUT requests to ADMIN and USER roles
                .requestMatchers(HttpMethod.PUT, API_PATTERN).hasAnyRole(ROLE_ADMIN, ROLE_USER)
                // Restrict DELETE requests to ADMIN role
                .requestMatchers(HttpMethod.DELETE, API_PATTERN).hasRole(ROLE_ADMIN)
                // Authenticate all other requests
                .anyRequest().authenticated()
                .and()
                .httpBasic(); // Use Basic Authentication for simplicity

        return http.build();
    }

    // In-memory authentication for demonstration purposes
    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(
                User.withUsername("admin")
                        .password(passwordEncoder().encode("admin123"))
                        .roles(ROLE_ADMIN)
                        .build()
        );
        manager.createUser(
                User.withUsername("user")
                        .password(passwordEncoder().encode("user123"))
                        .roles(ROLE_USER)
                        .build()
        );
        return manager;
    }
}