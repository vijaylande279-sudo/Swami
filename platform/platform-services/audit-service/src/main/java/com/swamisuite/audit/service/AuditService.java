package com.swamisuite.audit.service;

import com.swamisuite.audit.domain.AuditEvent;
import com.swamisuite.audit.domain.AuditEvent.ActorType;
import com.swamisuite.audit.dto.AuditDtos.AuditEventResponse;
import com.swamisuite.audit.dto.AuditDtos.RecordEventRequest;
import com.swamisuite.audit.repository.AuditEventRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    public void record(RecordEventRequest request) {
        AuditEvent event = new AuditEvent();
        event.setActorId(request.actorId());
        event.setActorType(ActorType.valueOf(request.actorType()));
        event.setTenantId(request.tenantId());
        event.setAction(request.action());
        event.setResourceType(request.resourceType());
        event.setResourceId(request.resourceId());
        event.setMetadata(request.metadata());
        repository.save(event);
    }

    public Page<AuditEventResponse> search(UUID tenantId, String action, Pageable pageable) {
        Page<AuditEvent> page;
        if (tenantId != null && action != null) {
            page = repository.findByTenantIdAndActionOrderByCreatedAtDesc(tenantId, action, pageable);
        } else if (tenantId != null) {
            page = repository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
        } else if (action != null) {
            page = repository.findByActionOrderByCreatedAtDesc(action, pageable);
        } else {
            page = repository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return page.map(this::toResponse);
    }

    private AuditEventResponse toResponse(AuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getActorId(), event.getActorType().name(),
                event.getTenantId(), event.getAction(), event.getResourceType(), event.getResourceId(),
                event.getMetadata(), event.getCreatedAt());
    }
}
