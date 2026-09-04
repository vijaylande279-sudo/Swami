package com.swamisuite.catalog.web;

import com.swamisuite.catalog.dto.CatalogDtos.AppSummary;
import com.swamisuite.catalog.dto.CatalogDtos.PlanSummary;
import com.swamisuite.catalog.service.CatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public - this is what the unauthenticated catalog/landing page reads. */
@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/apps")
    public List<AppSummary> listApps() {
        return catalogService.listApps();
    }

    @GetMapping("/apps/{appKey}/plan")
    public PlanSummary currentPlan(@PathVariable String appKey) {
        return catalogService.currentPlanForApp(appKey);
    }
}
