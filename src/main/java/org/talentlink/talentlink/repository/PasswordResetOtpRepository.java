package org.talentlink.talentlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.talentlink.talentlink.entity.PasswordResetOtp;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    // Lấy bản ghi OTP mới nhất theo email
    Optional<PasswordResetOtp> findTopByEmailIgnoreCaseOrderByCreatedAtDesc(String email);
}
