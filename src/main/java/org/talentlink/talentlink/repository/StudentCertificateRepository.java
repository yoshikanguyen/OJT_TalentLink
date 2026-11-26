package org.talentlink.talentlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.talentlink.talentlink.entity.StudentCertificate;

import java.util.List;
import java.util.Optional;

public interface StudentCertificateRepository extends JpaRepository<StudentCertificate, Long> {

    List<StudentCertificate> findAllByStudentProfileIdOrderByIssueDateDescIdDesc(Long studentProfileId);

    Optional<StudentCertificate> findByIdAndStudentProfileId(Long id, Long studentProfileId);
}
