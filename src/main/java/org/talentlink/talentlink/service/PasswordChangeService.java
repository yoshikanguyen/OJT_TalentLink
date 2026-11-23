package org.talentlink.talentlink.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.UserRepository;

@Service
public class PasswordChangeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordChangeService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu cũ.");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu mới.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản người dùng."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("Tài khoản hiện không hoạt động.");
        }

        // Kiểm tra mật khẩu cũ bằng BCrypt / PasswordEncoder
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác.");
        }

        // Tránh đổi trùng mật khẩu hiện tại (tùy chọn)
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
