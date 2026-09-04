package com.swamisuite.catalog.service;

import com.swamisuite.catalog.domain.App;
import com.swamisuite.catalog.domain.Plan;
import com.swamisuite.catalog.dto.CatalogDtos.AppSummary;
import com.swamisuite.catalog.dto.CatalogDtos.PlanSummary;
import com.swamisuite.catalog.repository.AppRepository;
import com.swamisuite.catalog.repository.PlanRepository;
import com.swamisuite.catalog.repository.TierRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {

    private final AppRepository appRepository;
    private final TierRepository tierRepository;
    private final PlanRepository planRepository;

    public CatalogService(AppRepository appRepository, TierRepository tierRepository, PlanRepository planRepository) {
        this.appRepository = appRepository;
        this.tierRepository = tierRepository;
        this.planRepository = planRepository;
    }

    public List<AppSummary> listApps() {
        return appRepository.findByActiveTrue().stream()
                .map(app -> new AppSummary(app.getKey(), app.getName(), app.getDescription(), currentPlan(app)))
                .toList();
    }

    public PlanSummary currentPlanForApp(String appKey) {
        App app = appRepository.findByKey(appKey).orElseThrow(() -> new CatalogException("Unknown app: " + appKey));
        PlanSummary plan = currentPlan(app);
        if (plan == null) {
            throw new CatalogException("No effective plan for app: " + appKey);
        }
        return plan;
    }

    /** For subscription-service's server-side price re-fetch - never trust a client-supplied plan snapshot. */
    public PlanSummary planById(UUID planId) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new CatalogException("Unknown plan: " + planId));
        return toSummary(plan);
    }

    private PlanSummary currentPlan(App app) {
        return tierRepository.findByAppId(app.getId()).stream()
                .flatMap(tier -> planRepository.findByTierId(tier.getId()).stream())
                .filter(Plan::isCurrentlyEffective)
                .max(Comparator.comparing(Plan::getEffectiveFrom))
                .map(this::toSummary)
                .orElse(null);
    }

    private PlanSummary toSummary(Plan plan) {
        return new PlanSummary(plan.getId(), plan.getPlanKey(), plan.getBillingInterval().name(),
                plan.getPricePaise(), plan.gstPaise(), plan.totalPaise());
    }

    public static class CatalogException extends RuntimeException {
        public CatalogException(String message) {
            super(message);
        }
    }
}
