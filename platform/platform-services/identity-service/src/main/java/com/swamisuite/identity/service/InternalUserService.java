package com.swamisuite.identity.service;

import com.swamisuite.identity.domain.Role;
import com.swamisuite.identity.domain.User;
import com.swamisuite.identity.dto.InternalDtos.CreateUserRequest;
import com.swamisuite.identity.dto.InternalDtos.UserSummary;
import com.swamisuite.identity.repository.RoleRepository;
import com.swamisuite.identity.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Backs POST /internal/users - tenant-service provisions the employee's account here on invite acceptance. */
@Service
public class InternalUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public InternalUserService(UserRepository userRepository, RoleRepository roleRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserSummary createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new AuthService.AuthException("Email already registered");
        }
        Role role = roleRepository.findByTenantIdAndName(request.tenantId(), request.roleName())
                .orElseThrow(() -> new AuthService.AuthException("Unknown role: " + request.roleName()));

        User user = new User(request.tenantId(), request.email().toLowerCase(),
                passwordEncoder.encode(request.password()), request.fullName());
        user.setRoles(Set.of(role));
        user = userRepository.save(user);

        return new UserSummary(user.getId(), user.getTenantId(), user.getEmail(), user.getFullName());
    }

    public List<UserSummary> listByTenant(UUID tenantId) {
        return userRepository.findByTenantId(tenantId).stream()
                .map(u -> new UserSummary(u.getId(), u.getTenantId(), u.getEmail(), u.getFullName()))
                .toList();
    }
}
