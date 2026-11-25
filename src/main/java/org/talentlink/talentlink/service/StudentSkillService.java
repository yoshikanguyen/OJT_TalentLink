package org.talentlink.talentlink.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talentlink.talentlink.entity.Skill;
import org.talentlink.talentlink.entity.StudentSkill;
import org.talentlink.talentlink.entity.StudentSkillId;
import org.talentlink.talentlink.repository.SkillRepository;
import org.talentlink.talentlink.repository.StudentSkillRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentSkillService {

    public static final Map<Integer, String> LEVEL_DESCRIPTIONS = Map.of(
            1, "Cơ bản — Nắm khái niệm nền tảng, làm được tác vụ đơn giản có hướng dẫn.",
            2, "Sơ cấp — Tự xử lý tác vụ cơ bản, còn cần hỗ trợ với bài toán phức tạp.",
            3, "Trung cấp — Làm việc độc lập, giải quyết tốt các tình huống phổ biến.",
            4, "Khá — Thành thạo, xử lý tốt edge case và tối ưu giải pháp.",
            5, "Chuyên sâu — Làm chủ kiến trúc/kỹ thuật, tối ưu cao và xử lý bài toán khó."
    );

    private final StudentSkillRepository studentSkillRepository;
    private final SkillRepository skillRepository;

    public List<Skill> findActiveSkills() {
        return skillRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .sorted(Comparator.comparing(Skill::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<StudentSkill> findByStudent(Long studentId) {
        return studentSkillRepository.findAllById_StudentIdOrderBySkill_NameAsc(studentId);
    }

    public Map<String, List<StudentSkill>> findGroupedByCategory(Long studentId) {
        return findByStudent(studentId).stream()
                .collect(Collectors.groupingBy(
                        ss -> (ss.getSkill().getCategory() == null || ss.getSkill().getCategory().isBlank())
                                ? "Khác"
                                : ss.getSkill().getCategory().trim(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    @Transactional
    public void addSkill(Long studentId, Long skillId, Integer proficiencyLevel) {
        validateLevel(proficiencyLevel);

        if (studentSkillRepository.existsById_StudentIdAndId_SkillId(studentId, skillId)) {
            throw new IllegalArgumentException("Kỹ năng này đã có trong hồ sơ của bạn.");
        }

        Skill skill = skillRepository.findById(skillId)
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .orElseThrow(() -> new IllegalArgumentException("Kỹ năng không tồn tại hoặc đã ngừng hoạt động."));

        StudentSkill entity = StudentSkill.builder()
                .id(new StudentSkillId(studentId, skillId))
                .skill(skill)
                .proficiencyLevel(proficiencyLevel)
                .build();

        try {
            studentSkillRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Kỹ năng này đã có trong hồ sơ của bạn.", ex);
        }
    }

    @Transactional
    public void updateLevel(Long studentId, Long skillId, Integer proficiencyLevel) {
        validateLevel(proficiencyLevel);

        StudentSkill existing = studentSkillRepository.findById_StudentIdAndId_SkillId(studentId, skillId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỹ năng trong hồ sơ."));

        existing.setProficiencyLevel(proficiencyLevel);
        studentSkillRepository.save(existing);
    }

    @Transactional
    public void removeSkill(Long studentId, Long skillId) {
        StudentSkill existing = studentSkillRepository.findById_StudentIdAndId_SkillId(studentId, skillId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỹ năng trong hồ sơ."));

        studentSkillRepository.delete(existing);
    }

    private void validateLevel(Integer level) {
        if (level == null || level < 1 || level > 5) {
            throw new IllegalArgumentException("Mức kỹ năng phải nằm trong khoảng từ 1 đến 5.");
        }
    }
}
