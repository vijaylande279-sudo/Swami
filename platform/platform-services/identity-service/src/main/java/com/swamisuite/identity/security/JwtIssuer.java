package com.swamisuite.identity.security;

import com.swamisuite.common.security.JwtProperties;
import com.swamisuite.identity.domain.Permission;
import com.swamisuite.identity.domain.Role;
import com.swamisuite.identity.domain.User;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Signs access tokens with identity-service's RSA private key. Never leaves this service. */
@Component
public class JwtIssuer {

    private final KeyPair keyPair;
    private final JwtProperties properties;

    public JwtIssuer(KeyPair keyPair, JwtProperties properties) {
        this.keyPair = keyPair;
        this.properties = properties;
    }

    public String issueAccessToken(User user) {
        List<String> roleNames = user.getRoles().stream().map(Role::getName).sorted().toList();
        Set<Permission> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .collect(Collectors.toSet());
        List<String> permCodes = permissions.stream().map(Permission::getCode).sorted().toList();

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.accessTokenTtlSeconds() > 0 ? properties.accessTokenTtlSeconds() : 900);

        var builder = Jwts.builder()
                .subject(user.getId().toString())
                .issuer(properties.issuer() != null ? properties.issuer() : "swami-suite-identity")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("roles", roleNames)
                .claim("perms", permCodes)
                // Entitlements come from subscription-service (Phase 2); always empty for now.
                .claim("entitlements", List.of());

        if (user.getTenantId() != null) {
            builder.claim("tenant_id", user.getTenantId().toString());
        }

        return builder.signWith(keyPair.getPrivate(), Jwts.SIG.RS256).compact();
    }

    public long refreshTokenTtlSeconds() {
        long ttl = properties.refreshTokenTtlSeconds();
        return ttl > 0 ? ttl : ChronoUnit.DAYS.getDuration().toSeconds() * 30;
    }
}
