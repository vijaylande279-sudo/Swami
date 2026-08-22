package com.hotel.oms.auth;

import com.hotel.oms.dto.auth.LoginRequest;
import com.hotel.oms.dto.auth.LoginResponse;
import com.hotel.oms.module.user.User;
import com.hotel.oms.module.user.UserRole;
import com.hotel.oms.module.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@hotel.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.WAITER);
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        when(userService.findByEmailOrThrow("alice@hotel.com")).thenReturn(user);
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken("alice@hotel.com", "WAITER")).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("alice@hotel.com", "plain-password"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo("WAITER");
        assertThat(response.name()).isEqualTo("Alice");
    }

    @Test
    void login_throwsBadCredentialsForWrongPassword() {
        when(userService.findByEmailOrThrow("alice@hotel.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice@hotel.com", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
