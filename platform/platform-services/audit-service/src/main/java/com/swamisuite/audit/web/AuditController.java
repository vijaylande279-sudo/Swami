package com.swamisuite.audit.web;

import com.swamisuite.audit.dto.AuditDtos.AuditEventResponse;
import com.swamisuite.audit.service.AuditService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The super admin console's audit viewer. */
@RestController
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/admin/audit")
    @PreAuthorize("hasAuthority('platform:audit:read')")
    public Page<AuditEventResponse> search(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) String action,
            Pageable pageable) {
        return auditService.search(tenantId, action, pageable);
    }
}
