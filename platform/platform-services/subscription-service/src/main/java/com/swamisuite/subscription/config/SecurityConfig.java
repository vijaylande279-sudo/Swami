package com.swamisuite.subscription.config;

import com.swamisuite.common.security.InternalTokenFilter;
import com.swamisuite.common.security.JwtAuthenticationFilter;
import com.swamisuite.common.security.JwtVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${swamisuite.services.internal-token:}")
    private String internalToken;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtVerifier jwtVerifier) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/internal/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtVerifier), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new InternalTokenFilter(internalToken), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
