package com.security.config;

import com.security.auth.CustomAuthenticationProvider;
import com.security.auth.JwtAuthenticationFilter;
import com.security.exception.CustomAccessDeniedHandler;
import com.security.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for setting up security configurations in the application.
 * @author Emir Aktaş
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationProvider authenticationProvider;


    /**
     * Configures the security filter chain for the application.
     *
     * @param http the HttpSecurity to modify
     * @return SecurityFilterChain the configured security filter chain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.trace("filterChain: Configuring security filter chain");

        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/user").hasRole("ADMIN")
                .requestMatchers("/auth/report/status/receipt").hasAnyRole("MANAGER", "CASHIER")
                .requestMatchers("/auth/report").hasRole("MANAGER")
                .requestMatchers("/auth/sale").hasRole("CASHIER")
                .requestMatchers("/auth/product").authenticated()
                .anyRequest().authenticated()
        );

        // NOTE: This line configures the application to not store session data on the server,
        // using stateless authentication, where each request must be authenticated individually.
        http.sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
        );

        log.trace("filterChain: Security filter chain configured successfully");
        return http.build();
    }
}
