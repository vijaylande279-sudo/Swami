package com.hotel.oms.module.user;

import com.hotel.oms.dto.auth.CreateUserRequest;
import com.hotel.oms.dto.auth.UserResponse;
import com.hotel.oms.exception.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void create_encodesPasswordAndPersistsUser() {
        when(userRepository.existsByEmail("kitchen@hotel.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.create(
                new CreateUserRequest("Kim", "kitchen@hotel.com", "password123", "KITCHEN"));

        assertThat(response.name()).isEqualTo("Kim");
        assertThat(response.role()).isEqualTo("KITCHEN");
    }

    @Test
    void create_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("kitchen@hotel.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(
                new CreateUserRequest("Kim", "kitchen@hotel.com", "password123", "KITCHEN")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void create_rejectsInvalidRole() {
        when(userRepository.existsByEmail("kitchen@hotel.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.create(
                new CreateUserRequest("Kim", "kitchen@hotel.com", "password123", "MANAGER")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid role");
    }
}
