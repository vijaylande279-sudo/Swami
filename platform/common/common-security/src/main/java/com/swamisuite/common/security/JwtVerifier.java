package com.swamisuite.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.List;

/**
 * Verifies an RS256 access token issued by identity-service and extracts its claims.
 * Never touches a private key - safe for any service to depend on. Registered as a
 * bean by {@link JwtVerificationAutoConfiguration}.
 */
public class JwtVerifier {

    private final PublicKeySource publicKeySource;

    public JwtVerifier(PublicKeySource publicKeySource) {
        this.publicKeySource = publicKeySource;
    }

    /**
     * @return the verified claims, or empty if the token is missing, malformed,
     * expired, or fails signature verification.
     */
    public java.util.Optional<JwtClaims> verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKeySource.getCurrentKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return java.util.Optional.of(toJwtClaims(claims));
        } catch (JwtException | IllegalArgumentException e) {
            publicKeySource.invalidate();
            return java.util.Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private JwtClaims toJwtClaims(Claims claims) {
        return new JwtClaims(
                claims.getSubject(),
                claims.get("tenant_id", String.class),
                claims.get("roles", List.class),
                claims.get("perms", List.class),
                claims.get("entitlements", List.class),
                claims.getExpiration().toInstant()
        );
    }
}
