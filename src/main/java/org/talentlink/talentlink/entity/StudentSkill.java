package org.talentlink.talentlink.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSkill {

    @EmbeddedId
    private StudentSkillId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "proficiency_level", nullable = false)
    private Integer proficiencyLevel;

    public Long getStudentId() {
        return id != null ? id.getStudentId() : null;
    }

    public Long getSkillId() {
        return id != null ? id.getSkillId() : null;
    }
}