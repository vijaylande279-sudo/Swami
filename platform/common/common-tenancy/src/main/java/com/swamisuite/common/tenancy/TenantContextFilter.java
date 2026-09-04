package com.swamisuite.common.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Populates {@link TenantContext} for the duration of a request.
 *
 * <p>Phase 0 scaffolding: reads a plain {@code X-Tenant-Id} header, since JWT
 * issuance/parsing does not exist yet (that lands with identity-service in Phase 1).
 * Once JWTs carry a {@code tenant_id} claim, this filter should read it from the
 * already-authenticated {@code JwtClaims} instead of a client-supplied header —
 * a client-supplied tenant header must never be trusted once real auth exists.
 */
public class TenantContextFilter extends GenericFilterBean implements Ordered {

    public static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {
            TenantContext.setTenantSchema(httpRequest.getHeader(TENANT_HEADER));
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
