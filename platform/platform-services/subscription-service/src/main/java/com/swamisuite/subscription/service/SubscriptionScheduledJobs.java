package com.swamisuite.subscription.service;

import com.swamisuite.subscription.domain.Subscription;
import com.swamisuite.subscription.domain.Subscription.SubscriptionStatus;
import com.swamisuite.subscription.repository.SubscriptionRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Annual-relevant scheduled jobs per doc §6.3/§15.5.2. The monthly-billing jobs
 * (renewal charge, dunning) don't apply - annual is a one-time payment with no
 * auto-debit to retry.
 */
@Component
public class SubscriptionScheduledJobs {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionScheduledJobs.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    public SubscriptionScheduledJobs(SubscriptionRepository subscriptionRepository, SubscriptionService subscriptionService) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Reminder ladder at T-30/15/7/1 before expiry, per doc §15.5.2. Dev-mode: logged
     * only, matching identity-service's LoggingResetLinkDeliverer pattern - no real
     * notification-service exists yet.
     */
    @Scheduled(cron = "0 0 10 * * *") // daily 10:00 (server-local time; IST scheduling is a later hardening item)
    @SchedulerLock(name = "subscription-expiry-reminders")
    public void sendExpiryReminders() {
        Instant now = Instant.now();
        for (int daysBefore : new int[] {30, 15, 7, 1}) {
            Instant windowStart = now.plus(daysBefore, ChronoUnit.DAYS);
            Instant windowEnd = windowStart.plus(1, ChronoUnit.DAYS);
            subscriptionRepository.findByStatusAndCurrentPeriodEndBetween(SubscriptionStatus.ACTIVE, windowStart, windowEnd)
                    .forEach(s -> log.info("DEV-MODE renewal reminder (T-{}d) for tenant {} app {}, expires {}",
                            daysBefore, s.getTenantId(), s.getAppKey(), s.getCurrentPeriodEnd()));
        }
    }

    /**
     * Moves subscriptions past their period end into PAST_DUE (starting the 7-day
     * grace window), then past grace into SUSPENDED with access actually revoked -
     * this is what proves the Phase 2 gate's "simulated failed renewal moves the
     * tenant to PAST_DUE and then SUSPENDED; access is blocked" requirement.
     */
    @Scheduled(cron = "0 0 3 * * *") // daily 03:00
    @SchedulerLock(name = "subscription-suspension-sweep")
    public void suspensionSweep() {
        Instant now = Instant.now();

        for (Subscription active : subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE)) {
            if (active.getCurrentPeriodEnd() != null && now.isAfter(active.getCurrentPeriodEnd())) {
                subscriptionService.markPastDue(active);
                log.info("Subscription {} (tenant {}, app {}) moved to PAST_DUE - period ended {}",
                        active.getId(), active.getTenantId(), active.getAppKey(), active.getCurrentPeriodEnd());
            }
        }

        for (Subscription pastDue : subscriptionRepository.findByStatusAndGraceUntilBefore(SubscriptionStatus.PAST_DUE, now)) {
            subscriptionService.suspend(pastDue);
            log.info("Subscription {} (tenant {}, app {}) moved to SUSPENDED - grace period expired",
                    pastDue.getId(), pastDue.getTenantId(), pastDue.getAppKey());
        }
    }
}
