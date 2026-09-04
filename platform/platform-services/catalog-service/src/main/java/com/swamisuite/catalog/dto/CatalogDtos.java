package com.swamisuite.catalog.dto;

import java.util.UUID;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record AppSummary(String key, String name, String description, PlanSummary plan) {
    }

    public record PlanSummary(UUID planId, String planKey, String billingInterval,
                               long pricePaise, long gstPaise, long totalPaise) {
    }
}
