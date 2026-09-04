package com.swamisuite.identity.config;

import com.swamisuite.common.security.JwtVerifier;
import com.swamisuite.common.security.PublicKeySource;
import com.swamisuite.common.security.StaticPublicKeySource;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** identity-service verifies its own tokens directly from the key pair it already holds. */
@Configuration
public class JwtVerificationConfig {

    @Bean
    public PublicKeySource publicKeySource(KeyPair keyPair) {
        return new StaticPublicKeySource((RSAPublicKey) keyPair.getPublic());
    }

    @Bean
    public JwtVerifier jwtVerifier(PublicKeySource publicKeySource) {
        return new JwtVerifier(publicKeySource);
    }
}
