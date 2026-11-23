package org.talentlink.talentlink.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talentlink.talentlink.dto.StudentRegisterForm;
import org.talentlink.talentlink.entity.StudentProfile;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.StudentProfileRepository;
import org.talentlink.talentlink.repository.UserRepository;

@Service
public class StudentRegistrationService {

    private static final Long STUDENT_ROLE_ID = 2L;

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentRegistrationService(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerStudent(StudentRegisterForm form) {
        String normalizedEmail = form.getEmail().trim().toLowerCase();

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email này đã được sử dụng");
        }

        User user = new User();
        user.setFullName(form.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRoleId(STUDENT_ROLE_ID);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        StudentProfile emptyPrivateProfile = new StudentProfile();
        emptyPrivateProfile.setUser(savedUser);
        emptyPrivateProfile.setPrivacyStatus("PRIVATE");
        studentProfileRepository.save(emptyPrivateProfile);

        return savedUser;
    }
}
