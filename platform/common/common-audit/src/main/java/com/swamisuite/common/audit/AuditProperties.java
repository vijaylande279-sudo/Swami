package com.swamisuite.common.audit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "swamisuite.services")
public record AuditProperties(String auditServiceUrl, String internalToken) {
}
