package org.talentlink.talentlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.talentlink.talentlink.entity.StudentProfile;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUser_Id(Long userId);
}
