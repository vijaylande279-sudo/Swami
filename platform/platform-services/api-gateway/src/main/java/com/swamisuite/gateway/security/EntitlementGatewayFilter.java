package com.swamisuite.gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Doc §6.2: reject any request to /api/{appKey}/** when appKey is not in the caller's
 * verified entitlements. Runs strictly after JwtAuthenticationGatewayFilter (higher
 * order value = later), reading the X-Entitlements header that filter set from
 * verified claims - never a client-supplied value, since that filter always strips
 * client-supplied identity headers before re-setting them.
 *
 * <p>Deliberately narrow in Phase 2: only /api/hello/** (the disposable hello-service
 * from Phase 0, reused as a stand-in "app") exists to protect, since no real
 * business-app service exists until Phase 4. Any future business-app service just
 * needs its own gateway route under /api/{itsAppKey}/** to be covered by this same
 * filter automatically.
 */
@Component
public class EntitlementGatewayFilter implements GlobalFilter, Ordered {

    private static final Pattern APP_PATH_PATTERN = Pattern.compile("^/api/([a-z0-9-]+)/.*");

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 200;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        Matcher matcher = APP_PATH_PATTERN.matcher(path);
        if (!matcher.matches()) {
            return chain.filter(exchange);
        }

        String appKey = matcher.group(1);
        List<String> entitlements = parseEntitlements(exchange);
        if (!entitlements.contains(appKey)) {
            return subscriptionInactive(exchange, appKey);
        }

        return chain.filter(exchange);
    }

    private List<String> parseEntitlements(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst("X-Entitlements");
        if (header == null || header.isBlank()) {
            return List.of();
        }
        return Arrays.asList(header.split(","));
    }

    private Mono<Void> subscriptionInactive(ServerWebExchange exchange, String appKey) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"code\":\"SUBSCRIPTION_INACTIVE\",\"message\":\"No active subscription for " + appKey + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
