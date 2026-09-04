package com.swamisuite.identity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Phase 1 dev-mode stub: logs the reset link instead of emailing it (notification-service is a later phase). */
@Component
public class LoggingResetLinkDeliverer implements ResetLinkDeliverer {

    private static final Logger log = LoggerFactory.getLogger(LoggingResetLinkDeliverer.class);

    @Override
    public void deliver(String email, String resetLink) {
        log.info("DEV-MODE password reset link for {}: {} (no email service configured yet)", email, resetLink);
    }
}
