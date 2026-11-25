package org.talentlink.talentlink.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talentlink.talentlink.dto.ProfileUpdateForm;
import org.talentlink.talentlink.entity.StudentProfile;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.StudentProfileRepository;
import org.talentlink.talentlink.repository.UserRepository;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    public ProfileService(UserRepository userRepository,
                          StudentProfileRepository studentProfileRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
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
        form.setPhone(user.getPhone());
        return form;
    }

    @Transactional
    public void updateProfile(String email, ProfileUpdateForm form) {
        User user = getCurrentUserByEmail(email);

        user.setFullName(form.getFullName().trim());
        user.setPhone(normalizeNullable(form.getPhone()));
        normalizeNullable(form.getBio());
        normalizeNullable(form.getDesiredPosition());
        normalizeNullable(form.getDesiredLocation());

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Long resolveStudentProfileIdByEmail(String email) {
        User user = getCurrentUserByEmail(email);
        return studentProfileRepository.findByUser_Id(user.getId())
                .map(StudentProfile::getId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tài khoản chưa có hồ sơ sinh viên (student_profiles)."
                ));
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}