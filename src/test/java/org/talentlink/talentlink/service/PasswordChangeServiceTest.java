package org.talentlink.talentlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordChangeServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private PasswordChangeService passwordChangeService;

    private User user;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        passwordChangeService = new PasswordChangeService(userRepository, passwordEncoder);

        user = new User();
        user.setId(1L);
        user.setEmail("ethan.student@talentlink.local");
        user.setPasswordHash(passwordEncoder.encode("Current@123"));
        user.setIsActive(true);
    }

    @Test
    void changePassword_wrongCurrentPassword_shouldThrowExceptionAndNotSave() {
        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordChangeService.changePassword(
                        user.getEmail(),
                        "WrongOld@123",
                        "NewPass@123",
                        "NewPass@123"
                )
        );

        assertTrue(ex.getMessage().toLowerCase().contains("mật khẩu cũ"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_validCurrentPassword_shouldUpdatePasswordHash() {
        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        passwordChangeService.changePassword(
                user.getEmail(),
                "Current@123",
                "NewPass@123",
                "NewPass@123"
        );

        assertTrue(passwordEncoder.matches("NewPass@123", user.getPasswordHash()));
        verify(userRepository).save(user);
    }
}
