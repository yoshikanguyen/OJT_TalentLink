package org.talentlink.talentlink.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.talentlink.talentlink.dto.ProfileUpdateForm;
import org.talentlink.talentlink.entity.StudentProfile;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.StudentProfileRepository;
import org.talentlink.talentlink.repository.UserRepository;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AvatarStorageService avatarStorageService;

    public ProfileService(UserRepository userRepository,
                          StudentProfileRepository studentProfileRepository,
                          AvatarStorageService avatarStorageService) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.avatarStorageService = avatarStorageService;
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
        String oldAvatarUrl = user.getAvatarUrl();

        user.setFullName(form.getFullName().trim());
        user.setDateOfBirth(form.getDateOfBirth());
        user.setPhone(normalizeNullable(form.getPhone()));
        user.setBio(normalizeNullable(form.getBio()));
        user.setDesiredPosition(normalizeNullable(form.getDesiredPosition()));
        user.setDesiredLocation(normalizeNullable(form.getDesiredLocation()));

        MultipartFile avatarFile = form.getAvatarFile();
        boolean hasNewAvatar = avatarFile != null && !avatarFile.isEmpty();

        if (hasNewAvatar) {
            String newAvatarUrl = avatarStorageService.saveAvatar(avatarFile);
            user.setAvatarUrl(newAvatarUrl);
            userRepository.save(user);
            avatarStorageService.deleteAvatarIfExists(oldAvatarUrl);
        } else if (form.isRemoveAvatar()) {
            user.setAvatarUrl(null);
            userRepository.save(user);
            avatarStorageService.deleteAvatarIfExists(oldAvatarUrl);
        } else {
            userRepository.save(user);
        }
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