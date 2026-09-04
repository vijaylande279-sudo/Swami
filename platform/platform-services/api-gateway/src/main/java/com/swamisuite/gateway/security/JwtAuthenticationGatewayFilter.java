package com.swamisuite.gateway.security;

import com.swamisuite.common.security.JwtClaims;
import com.swamisuite.common.security.JwtVerifier;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Phase 1 authentication-only gateway filter (§6.2's entitlement check is explicitly
 * Phase 2, not here). Whitelists public auth paths; everything else needs a valid
 * Bearer token. Always strips any client-supplied identity headers before verifying,
 * then sets them fresh from the verified claims - never trust-then-forward.
 */
@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/auth/register", "/auth/login", "/auth/login/mfa", "/auth/refresh",
            "/auth/password/forgot", "/auth/password/reset",
            "/.well-known/jwks.json"
    );
    private static final List<String> IDENTITY_HEADERS = List.of("X-User-Id", "X-Tenant-Id", "X-Roles", "X-Permissions");

    private final JwtVerifier jwtVerifier;

    public JwtAuthenticationGatewayFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        ServerHttpRequest.Builder strippedRequest = exchange.getRequest().mutate();
        IDENTITY_HEADERS.forEach(h -> strippedRequest.headers(headers -> headers.remove(h)));

        if (isPublic(path) || path.startsWith("/actuator")) {
            return chain.filter(exchange.mutate().request(strippedRequest.build()).build());
        }

        String token = extractBearerToken(exchange);
        if (token == null) {
            return unauthorized(exchange, "Missing bearer token");
        }

        return Mono.fromCallable(() -> jwtVerifier.verify(token))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(maybeClaims -> maybeClaims
                        .map(claims -> chain.filter(exchange.mutate()
                                .request(withIdentityHeaders(strippedRequest, claims).build())
                                .build()))
                        .orElseGet(() -> unauthorized(exchange, "Invalid or expired token")));
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.contains(path);
    }

    private String extractBearerToken(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private ServerHttpRequest.Builder withIdentityHeaders(ServerHttpRequest.Builder builder, JwtClaims claims) {
        builder.header("X-User-Id", claims.subject());
        if (claims.tenantId() != null) {
            builder.header("X-Tenant-Id", claims.tenantId());
        }
        if (claims.roles() != null) {
            builder.header("X-Roles", String.join(",", claims.roles()));
        }
        if (claims.permissions() != null) {
            builder.header("X-Permissions", String.join(",", claims.permissions()));
        }
        return builder;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"code\":\"UNAUTHENTICATED\",\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
