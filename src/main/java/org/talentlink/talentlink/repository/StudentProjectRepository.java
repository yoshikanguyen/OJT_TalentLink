package org.talentlink.talentlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.talentlink.talentlink.entity.StudentProject;

import java.util.List;
import java.util.Optional;

public interface StudentProjectRepository extends JpaRepository<StudentProject, Long> {

    List<StudentProject> findAllByStudentIdOrderByIdDesc(Long studentId);

    long countByStudentId(Long studentId);

    Optional<StudentProject> findByIdAndStudentId(Long id, Long studentId);
}
