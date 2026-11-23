package org.talentlink.talentlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.talentlink.talentlink.entity.PasswordResetOtp;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.PasswordResetOtpRepository;
import org.talentlink.talentlink.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetOtpRepository otpRepository;

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private PasswordResetService passwordResetService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        passwordResetService = new PasswordResetService(otpRepository, userRepository, passwordEncoder);

        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("ethan.student@talentlink.local");
        activeUser.setPasswordHash(passwordEncoder.encode("OldPass@123"));
        activeUser.setIsActive(true);
    }

    @Test
    void requestOtp_validEmail_shouldCreateSixDigitOtpWithTenMinuteExpiration() {
        when(otpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc("ethan.student@talentlink.local"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("ethan.student@talentlink.local"))
                .thenReturn(Optional.of(activeUser));

        LocalDateTime beforeCall = LocalDateTime.now();
        passwordResetService.requestOtp("Ethan.Student@talentlink.local ");
        LocalDateTime afterCall = LocalDateTime.now();

        ArgumentCaptor<PasswordResetOtp> captor = ArgumentCaptor.forClass(PasswordResetOtp.class);
        verify(otpRepository).save(captor.capture());

        PasswordResetOtp savedOtp = captor.getValue();
        assertEquals("ethan.student@talentlink.local", savedOtp.getEmail());
        assertNotNull(savedOtp.getOtpCode());
        assertTrue(savedOtp.getOtpCode().matches("\\d{6}"), "OTP phải gồm đúng 6 chữ số");
        assertEquals(0, savedOtp.getFailedAttempts());
        assertFalse(savedOtp.getIsUsed());
        assertFalse(savedOtp.getIsCancelled());

        assertFalse(savedOtp.getExpiresAt().isBefore(beforeCall.plusMinutes(10).minusSeconds(2)));
        assertFalse(savedOtp.getExpiresAt().isAfter(afterCall.plusMinutes(10).plusSeconds(2)));
    }

    @Test
    void resetPasswordWithOtp_expiredAfterTenMinutes_shouldCancelAndThrowException() {
        PasswordResetOtp expiredOtp = new PasswordResetOtp();
        expiredOtp.setUser(activeUser);
        expiredOtp.setEmail(activeUser.getEmail());
        expiredOtp.setOtpCode("123456");
        expiredOtp.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        expiredOtp.setFailedAttempts(0);
        expiredOtp.setIsUsed(false);
        expiredOtp.setIsCancelled(false);

        when(otpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(activeUser.getEmail()))
                .thenReturn(Optional.of(expiredOtp));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPasswordWithOtp(activeUser.getEmail(), "123456", "NewPass@123")
        );

        assertTrue(ex.getMessage().contains("hết hạn"));
        assertTrue(expiredOtp.getIsCancelled());
        verify(otpRepository).save(expiredOtp);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPasswordWithOtp_alreadyUsedOtp_shouldRejectReuse() {
        PasswordResetOtp usedOtp = new PasswordResetOtp();
        usedOtp.setUser(activeUser);
        usedOtp.setEmail(activeUser.getEmail());
        usedOtp.setOtpCode("654321");
        usedOtp.setExpiresAt(LocalDateTime.now().plusMinutes(8));
        usedOtp.setFailedAttempts(0);
        usedOtp.setIsUsed(true);
        usedOtp.setIsCancelled(false);

        when(otpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(activeUser.getEmail()))
                .thenReturn(Optional.of(usedOtp));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPasswordWithOtp(activeUser.getEmail(), "654321", "NewPass@123")
        );

        assertTrue(ex.getMessage().contains("đã được sử dụng"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPasswordWithOtp_wrongOtpMoreThanFiveTimes_shouldCancelOtp() {
        PasswordResetOtp otpRecord = new PasswordResetOtp();
        otpRecord.setUser(activeUser);
        otpRecord.setEmail(activeUser.getEmail());
        otpRecord.setOtpCode("999999");
        otpRecord.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRecord.setFailedAttempts(5); // Đã sai 5 lần trước đó, lần này là lần sai thứ 6 (quá 5 lần)
        otpRecord.setIsUsed(false);
        otpRecord.setIsCancelled(false);

        when(otpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(activeUser.getEmail()))
                .thenReturn(Optional.of(otpRecord));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPasswordWithOtp(activeUser.getEmail(), "000000", "NewPass@123")
        );

        assertEquals(6, otpRecord.getFailedAttempts());
        assertTrue(otpRecord.getIsCancelled());
        assertTrue(ex.getMessage().contains("quá 5 lần"));
        verify(otpRepository).save(otpRecord);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPasswordWithOtp_validOtp_shouldUpdatePasswordAndMarkUsedOnce() {
        PasswordResetOtp validOtp = new PasswordResetOtp();
        validOtp.setUser(activeUser);
        validOtp.setEmail(activeUser.getEmail());
        validOtp.setOtpCode("482910");
        validOtp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        validOtp.setFailedAttempts(0);
        validOtp.setIsUsed(false);
        validOtp.setIsCancelled(false);

        when(otpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(activeUser.getEmail()))
                .thenReturn(Optional.of(validOtp));

        passwordResetService.resetPasswordWithOtp(activeUser.getEmail(), "482910", "NewPass@123");

        assertTrue(validOtp.getIsUsed());
        assertTrue(passwordEncoder.matches("NewPass@123", activeUser.getPasswordHash()));
        verify(userRepository).save(activeUser);
        verify(otpRepository).save(validOtp);
    }
}
