package org.talentlink.talentlink.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talentlink.talentlink.entity.Company;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.CompanyRepository;
import org.talentlink.talentlink.repository.UserRepository;
import org.talentlink.talentlink.security.RoleMappingHelper;

import java.time.LocalDateTime;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<User> getUsers(String keyword, Long roleId, String status, int page, int size) {
        String normalizedKeyword = keyword != null ? keyword.trim() : null;
        Boolean isActive = parseStatus(status);
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? Math.min(size, 50) : 10;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return userRepository.searchUsers(normalizedKeyword, roleId, isActive, pageable);
    }

    @Transactional
    public void createPartnerAccount(
            String fullName,
            String email,
            String rawPassword,
            String phone,
            String companyName
    ) {
        String cleanEmail = email != null ? email.trim() : "";
        String cleanFullName = fullName != null ? fullName.trim() : "";
        String cleanCompanyName = companyName != null ? companyName.trim() : "";
        String cleanPhone = phone != null && !phone.isBlank() ? phone.trim() : null;

        if (cleanFullName.isEmpty() || cleanEmail.isEmpty() || rawPassword == null || rawPassword.isBlank() || cleanCompanyName.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ họ tên, email, mật khẩu và tên công ty.");
        }

        if (userRepository.findByEmailIgnoreCase(cleanEmail).isPresent()) {
            throw new IllegalArgumentException("Email này đã tồn tại trong hệ thống.");
        }

        companyRepository.findByNameIgnoreCase(cleanCompanyName)
                .orElseGet(() -> {
                    Company company = new Company();
                    company.setName(cleanCompanyName);
                    return companyRepository.save(company);
                });

        User partner = new User();
        partner.setFullName(cleanFullName);
        partner.setEmail(cleanEmail);
        partner.setPasswordHash(passwordEncoder.encode(rawPassword));
        partner.setPhone(cleanPhone);
        partner.setRoleId(RoleMappingHelper.PARTNER_ROLE_ID);
        partner.setIsActive(true);
        partner.setCreatedAt(LocalDateTime.now());

        userRepository.save(partner);
    }

    @Transactional
    public boolean toggleUserActiveStatus(Long targetUserId, String currentAdminEmail) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));

        boolean currentlyActive = Boolean.TRUE.equals(targetUser.getIsActive());
        boolean willLock = currentlyActive;

        if (willLock && currentAdminEmail != null && targetUser.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new IllegalStateException("Bạn không thể tự khoá tài khoản của chính mình.");
        }

        boolean nextActive = !currentlyActive;
        targetUser.setIsActive(nextActive);
        userRepository.save(targetUser);
        return nextActive;
    }

    private Boolean parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return switch (status.trim().toLowerCase()) {
            case "active" -> Boolean.TRUE;
            case "locked" -> Boolean.FALSE;
            default -> null;
        };
    }
}
