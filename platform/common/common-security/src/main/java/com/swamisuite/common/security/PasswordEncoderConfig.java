package com.swamisuite.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Registered unconditionally for every common-security consumer via
 * AutoConfiguration.imports, but {@code @ConditionalOnClass} makes it a genuine
 * no-op for api-gateway, which deliberately excludes spring-boot-starter-security
 * entirely (WebFlux-only, see api-gateway/pom.xml) - without this guard, Spring
 * would fail trying to classload BCryptPasswordEncoder there.
 */
@Configuration
@ConditionalOnClass(PasswordEncoder.class)
public class PasswordEncoderConfig {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
