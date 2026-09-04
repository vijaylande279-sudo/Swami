package com.swamisuite.catalog.web;

import com.swamisuite.catalog.dto.CatalogDtos.PlanSummary;
import com.swamisuite.catalog.service.CatalogService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Service-to-service only - guarded by InternalTokenFilter, never routed through the gateway. */
@RestController
@RequestMapping("/internal/catalog")
public class InternalCatalogController {

    private final CatalogService catalogService;

    public InternalCatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/plans/{planId}")
    public PlanSummary planById(@PathVariable UUID planId) {
        return catalogService.planById(planId);
    }
}
