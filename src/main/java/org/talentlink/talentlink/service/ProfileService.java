package org.talentlink.talentlink.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talentlink.talentlink.dto.ProfileUpdateForm;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.UserRepository;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getCurrentUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Không xác định được tài khoản đang đăng nhập.");
        }
        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ người dùng."));
    }

    @Transactional(readOnly = true)
    public ProfileUpdateForm getProfileForm(String email) {
        User user = getCurrentUserByEmail(email);
        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setFullName(user.getFullName());
        form.setDateOfBirth(user.getDateOfBirth());
        form.setPhone(user.getPhone());
        form.setBio(user.getBio());
        form.setDesiredPosition(user.getDesiredPosition());
        form.setDesiredLocation(user.getDesiredLocation());
        return form;
    }

    @Transactional
    public void updateProfile(String email, ProfileUpdateForm form) {
        User user = getCurrentUserByEmail(email);

        user.setFullName(form.getFullName().trim());
        user.setDateOfBirth(form.getDateOfBirth());
        user.setPhone(normalizeNullable(form.getPhone()));
        user.setBio(normalizeNullable(form.getBio()));
        user.setDesiredPosition(normalizeNullable(form.getDesiredPosition()));
        user.setDesiredLocation(normalizeNullable(form.getDesiredLocation()));

        userRepository.save(user);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
