package org.talentlink.talentlink.service;

import org.talentlink.talentlink.entity.Skill;
import org.talentlink.talentlink.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillService {

    private final SkillRepository skillRepository;

    public List<Skill> findAll() {
        return skillRepository.findAllByOrderByIdAsc();
    }

    public Skill findById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỹ năng với ID: " + id));
    }

    @Transactional
    public Skill create(String name, String category) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Tên kỹ năng không được để trống.");
        }
        if (skillRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Kỹ năng '" + normalizedName + "' đã tồn tại.");
        }

        String normalizedCategory = (category == null || category.isBlank()) ? null : category.trim();

        Skill skill = Skill.builder()
                .name(normalizedName)
                .category(normalizedCategory)
                .isActive(true)
                .build();
        return skillRepository.save(skill);
    }

    @Transactional
    public Skill update(Long id, String name, String category) {
        Skill existing = findById(id);
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Tên kỹ năng không được để trống.");
        }
        if (skillRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new IllegalArgumentException("Kỹ năng '" + normalizedName + "' đã tồn tại.");
        }

        existing.setName(normalizedName);
        existing.setCategory((category == null || category.isBlank()) ? null : category.trim());
        return skillRepository.save(existing);
    }

    @Transactional
    public void toggleActive(Long id) {
        Skill existing = findById(id);
        existing.setIsActive(!Boolean.TRUE.equals(existing.getIsActive()));
        skillRepository.save(existing);
    }
}
