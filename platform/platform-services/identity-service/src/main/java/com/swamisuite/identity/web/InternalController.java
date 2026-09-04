package com.swamisuite.identity.web;

import com.swamisuite.identity.dto.InternalDtos.CreateUserRequest;
import com.swamisuite.identity.dto.InternalDtos.UserSummary;
import com.swamisuite.identity.service.InternalUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Service-to-service only - guarded by InternalTokenFilter, never routed through the gateway. */
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final InternalUserService internalUserService;

    public InternalController(InternalUserService internalUserService) {
        this.internalUserService = internalUserService;
    }

    @PostMapping("/users")
    public UserSummary createUser(@Valid @RequestBody CreateUserRequest request) {
        return internalUserService.createUser(request);
    }

    @GetMapping("/users")
    public List<UserSummary> listByTenant(@RequestParam UUID tenantId) {
        return internalUserService.listByTenant(tenantId);
    }
}
