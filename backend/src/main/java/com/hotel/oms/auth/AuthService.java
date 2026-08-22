package com.hotel.oms.auth;

import com.hotel.oms.dto.auth.CreateUserRequest;
import com.hotel.oms.dto.auth.LoginRequest;
import com.hotel.oms.dto.auth.LoginResponse;
import com.hotel.oms.dto.auth.RegisterRequest;
import com.hotel.oms.dto.auth.UserResponse;
import com.hotel.oms.module.user.User;
import com.hotel.oms.module.user.UserRole;
import com.hotel.oms.module.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userService.findByEmailOrThrow(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, user.getRole().name(), user.getName());
    }

    public UserResponse me(String email) {
        User user = userService.findByEmailOrThrow(email);
        return userService.toResponse(user);
    }

    public LoginResponse register(RegisterRequest request) {
        CreateUserRequest createRequest = new CreateUserRequest(
                request.name(), request.email(), request.password(), UserRole.WAITER.name());
        UserResponse created = userService.create(createRequest);

        String token = jwtUtil.generateToken(created.email(), created.role());
        return new LoginResponse(token, created.role(), created.name());
    }
}
