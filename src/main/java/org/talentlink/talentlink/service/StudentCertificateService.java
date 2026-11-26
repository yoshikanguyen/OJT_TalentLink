package org.talentlink.talentlink.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talentlink.talentlink.dto.StudentCertificateForm;
import org.talentlink.talentlink.entity.StudentCertificate;
import org.talentlink.talentlink.repository.StudentCertificateRepository;
import org.talentlink.talentlink.security.CustomUserDetails;
import org.talentlink.talentlink.security.RoleMappingHelper;

import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentCertificateService {

    private final StudentCertificateRepository certificateRepository;
    private final CertificateStorageService storageService;
    private final ProfileService profileService;

    public record CertificateFileDownload(
            StudentCertificate certificate,
            Path filePath
    ) {}

    @Transactional(readOnly = true)
    public List<StudentCertificate> getCertificatesByStudentProfileId(Long studentProfileId) {
        return certificateRepository.findAllByStudentProfileIdOrderByIssueDateDescIdDesc(studentProfileId);
    }

    @Transactional
    public StudentCertificate createCertificate(Long studentProfileId, StudentCertificateForm form) {
        CertificateStorageService.StoredCertificateFile stored = storageService.storeCertificateFile(form.getProofFile());

        StudentCertificate certificate = StudentCertificate.builder()
                .studentProfileId(studentProfileId)
                .certificateName(form.getCertificateName().trim())
                .issuingOrganization(form.getIssuingOrganization().trim())
                .issueDate(form.getIssueDate())
                .storedFileName(stored.storedFileName())
                .originalFileName(stored.originalFileName())
                .contentType(stored.contentType())
                .fileSizeBytes(stored.fileSizeBytes())
                .build();

        try {
            return certificateRepository.save(certificate);
        } catch (RuntimeException ex) {
            storageService.deleteFileIfExists(stored.storedFileName());
            throw ex;
        }
    }

    @Transactional
    public void deleteCertificate(Long certificateId, Long studentProfileId) {
        StudentCertificate certificate = certificateRepository
                .findByIdAndStudentProfileId(certificateId, studentProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chứng chỉ hoặc bạn không có quyền xoá."));

        String storedFileName = certificate.getStoredFileName();
        certificateRepository.delete(certificate);
        storageService.deleteFileIfExists(storedFileName);
    }

    @Transactional(readOnly = true)
    public CertificateFileDownload getCertificateFileForViewer(Long certificateId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn cần đăng nhập để xem file minh chứng.");
        }

        StudentCertificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chứng chỉ."));

        if (!isAdmin(authentication) && !isOwner(certificate.getStudentProfileId(), authentication)) {
            throw new AccessDeniedException("Chỉ chủ hồ sơ và quản trị viên mới có quyền xem/tải file minh chứng.");
        }

        Path filePath = storageService.resolveExistingFile(certificate.getStoredFileName());
        return new CertificateFileDownload(certificate, filePath);
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails customUser) {
            if (Long.valueOf(RoleMappingHelper.ADMIN_ROLE_ID).equals(customUser.getRoleId())) {
                return true;
            }
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private boolean isOwner(Long certificateStudentProfileId, Authentication authentication) {
        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            return false;
        }
        try {
            Long currentStudentProfileId = profileService.resolveStudentProfileIdByEmail(email);
            return certificateStudentProfileId.equals(currentStudentProfileId);
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
