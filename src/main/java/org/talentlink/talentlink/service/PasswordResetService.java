package org.talentlink.talentlink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talentlink.talentlink.entity.PasswordResetOtp;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.PasswordResetOtpRepository;
import org.talentlink.talentlink.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PasswordResetOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            PasswordResetOtpRepository otpRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Bước 1: Nhập email -> sinh mã OTP 6 số lưu DB, hết hạn sau 10 phút, ghi mã ra log.
     */
    @Transactional
    public void requestOtp(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        LocalDateTime now = LocalDateTime.now();

        // Hủy OTP gần nhất nếu vẫn đang mở (chưa dùng và chưa hủy)
        otpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .ifPresent(existing -> {
                    if (!Boolean.TRUE.equals(existing.getIsUsed()) && !Boolean.TRUE.equals(existing.getIsCancelled())) {
                        existing.setIsCancelled(true);
                        otpRepository.save(existing);
                    }
                });

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            log.info("[PASSWORD_RESET_OTP] Yêu cầu cho email không tồn tại: {}", email);
            return;
        }

        User user = userOpt.get();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("[PASSWORD_RESET_OTP] Tài khoản đang bị vô hiệu hóa (is_active = false): {}", email);
            return;
        }

        String otpCode = generateSixDigitOtp();

        PasswordResetOtp otpEntity = new PasswordResetOtp();
        otpEntity.setUser(user);
        otpEntity.setEmail(email);
        otpEntity.setOtpCode(otpCode);
        otpEntity.setExpiresAt(now.plusMinutes(OTP_EXPIRATION_MINUTES));
        otpEntity.setFailedAttempts(0);
        otpEntity.setIsUsed(false);
        otpEntity.setIsCancelled(false);

        otpRepository.save(otpEntity);

        // Ghi mã OTP ra log thay cho việc gửi mail
        log.info("[PASSWORD_RESET_OTP] Email: {}, OTP: {}, Hết hạn lúc: {}",
                email, otpCode, otpEntity.getExpiresAt());
    }

    /**
     * Bước 2: Nhập mã OTP + mật khẩu mới để đặt lại mật khẩu.
     * - Mã dùng một lần
     * - Hết hạn sau 10 phút
     * - Sai quá 5 lần thì hủy mã
     */
    @Transactional
    public void resetPasswordWithOtp(String rawEmail, String rawOtpCode, String newPassword) {
        String email = normalizeEmail(rawEmail);
        String otpInput = rawOtpCode == null ? "" : rawOtpCode.trim();
        LocalDateTime now = LocalDateTime.now();

        PasswordResetOtp otpRecord = otpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Mã OTP không hợp lệ hoặc không tồn tại."));

        if (Boolean.TRUE.equals(otpRecord.getIsUsed())) {
            throw new IllegalArgumentException("Mã OTP này đã được sử dụng. Vui lòng yêu cầu mã mới.");
        }

        if (Boolean.TRUE.equals(otpRecord.getIsCancelled())) {
            throw new IllegalArgumentException("Mã OTP đã bị hủy. Vui lòng yêu cầu mã mới.");
        }

        if (now.isAfter(otpRecord.getExpiresAt())) {
            otpRecord.setIsCancelled(true);
            otpRepository.save(otpRecord);
            throw new IllegalArgumentException("Mã OTP đã hết hạn sau 10 phút. Vui lòng yêu cầu mã mới.");
        }

        if (!otpRecord.getOtpCode().equals(otpInput)) {
            int nextAttempts = (otpRecord.getFailedAttempts() == null ? 0 : otpRecord.getFailedAttempts()) + 1;
            otpRecord.setFailedAttempts(nextAttempts);

            if (nextAttempts > MAX_FAILED_ATTEMPTS) {
                otpRecord.setIsCancelled(true);
                otpRepository.save(otpRecord);
                log.warn("[PASSWORD_RESET_OTP] Hủy OTP của email {} do nhập sai quá {} lần.", email, MAX_FAILED_ATTEMPTS);
                throw new IllegalArgumentException("Bạn đã nhập sai mã OTP quá 5 lần. Mã đã bị hủy, vui lòng yêu cầu mã mới.");
            }

            otpRepository.save(otpRecord);
            int remaining = MAX_FAILED_ATTEMPTS - nextAttempts + 1;
            throw new IllegalArgumentException("Mã OTP không chính xác. Bạn còn " + remaining + " lần thử.");
        }

        User user = otpRecord.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("Tài khoản hiện không hoạt động.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpRecord.setIsUsed(true);
        otpRepository.save(otpRecord);

        log.info("[PASSWORD_RESET_OTP] Đặt lại mật khẩu thành công cho email: {}", email);
    }

    private String generateSixDigitOtp() {
        int number = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
