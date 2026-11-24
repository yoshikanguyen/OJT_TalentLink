package org.talentlink.talentlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.talentlink.talentlink.entity.Education;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {

    List<Education> findByUserIdOrderByStartDateDescIdDesc(Long userId);
}
