package com.swamisuite.catalog.config;

import com.swamisuite.common.security.InternalTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * catalog-service has no JWT-protected endpoints in Phase 2 (no admin write UI yet -
 * pricing is seeded via migration only, per the Phase 2 plan) - just a public read API
 * and an internal-token-guarded service-to-service one.
 */
@Configuration
public class SecurityConfig {

    @Value("${swamisuite.services.internal-token:}")
    private String internalToken;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new InternalTokenFilter(internalToken), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
