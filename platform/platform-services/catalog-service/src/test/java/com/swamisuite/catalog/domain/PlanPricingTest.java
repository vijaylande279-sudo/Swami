package com.swamisuite.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Confirms the doc's exact quoted totals: Restaurant/Coffee/Bar ₹50,000 + 18% = ₹59,000; Hotel ₹1,00,000 + 18% = ₹1,18,000. */
class PlanPricingTest {

    @Test
    void standardTierAnnualPlan_matchesDocQuotedTotal() {
        Plan plan = new Plan(UUID.randomUUID(), "restaurant-annual", 5_000_000L, 1800);

        assertThat(plan.gstPaise()).isEqualTo(900_000L);
        assertThat(plan.totalPaise()).isEqualTo(5_900_000L); // ₹59,000
    }

    @Test
    void largeTierAnnualPlan_matchesDocQuotedTotal() {
        Plan plan = new Plan(UUID.randomUUID(), "hotel-annual", 10_000_000L, 1800);

        assertThat(plan.gstPaise()).isEqualTo(1_800_000L);
        assertThat(plan.totalPaise()).isEqualTo(11_800_000L); // ₹1,18,000
    }

    @Test
    void isCurrentlyEffective_falseBeforeEffectiveFrom() {
        Plan plan = new Plan(UUID.randomUUID(), "future-plan", 1000, 1800);
        plan.setEffectiveFrom(java.time.Instant.now().plusSeconds(3600));

        assertThat(plan.isCurrentlyEffective()).isFalse();
    }

    @Test
    void isCurrentlyEffective_falseAfterEffectiveTo() {
        Plan plan = new Plan(UUID.randomUUID(), "expired-plan", 1000, 1800);
        plan.setEffectiveFrom(java.time.Instant.now().minusSeconds(7200));
        plan.setEffectiveTo(java.time.Instant.now().minusSeconds(3600));

        assertThat(plan.isCurrentlyEffective()).isFalse();
    }
}
