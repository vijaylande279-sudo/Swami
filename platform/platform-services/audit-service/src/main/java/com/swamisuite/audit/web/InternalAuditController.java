package com.swamisuite.audit.web;

import com.swamisuite.audit.dto.AuditDtos.RecordEventRequest;
import com.swamisuite.audit.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Service-to-service only - guarded by InternalTokenFilter, never routed through the gateway. Called by AuditClient (common-audit) from every other service. */
@RestController
@RequestMapping("/internal/audit")
public class InternalAuditController {

    private final AuditService auditService;

    public InternalAuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public void record(@Valid @RequestBody RecordEventRequest request) {
        auditService.record(request);
    }
}
