package org.talentlink.talentlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.talentlink.talentlink.entity.StudentSkill;
import org.talentlink.talentlink.entity.StudentSkillId;

import java.util.List;
import java.util.Optional;

public interface StudentSkillRepository extends JpaRepository<StudentSkill, StudentSkillId> {

    List<StudentSkill> findAllById_StudentIdOrderBySkill_NameAsc(Long studentId);

    Optional<StudentSkill> findById_StudentIdAndId_SkillId(Long studentId, Long skillId);

    boolean existsById_StudentIdAndId_SkillId(Long studentId, Long skillId);
}
