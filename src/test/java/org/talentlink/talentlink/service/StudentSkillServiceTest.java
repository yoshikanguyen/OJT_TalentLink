package org.talentlink.talentlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.talentlink.talentlink.entity.Skill;
import org.talentlink.talentlink.entity.StudentSkill;
import org.talentlink.talentlink.entity.StudentSkillId;
import org.talentlink.talentlink.repository.SkillRepository;
import org.talentlink.talentlink.repository.StudentSkillRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentSkillServiceTest {

    @Mock
    private StudentSkillRepository studentSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private StudentSkillService studentSkillService;

    private Skill activeJavaSkill;
    private Skill activeSpringSkill;
    private Skill inactiveSkill;

    @BeforeEach
    void setUp() {
        activeJavaSkill = new Skill();
        activeJavaSkill.setId(10L);
        activeJavaSkill.setName("Java");
        activeJavaSkill.setCategory("Backend");
        activeJavaSkill.setIsActive(true);

        activeSpringSkill = new Skill();
        activeSpringSkill.setId(20L);
        activeSpringSkill.setName("Spring Boot");
        activeSpringSkill.setCategory("Backend");
        activeSpringSkill.setIsActive(true);

        inactiveSkill = new Skill();
        inactiveSkill.setId(99L);
        inactiveSkill.setName("Legacy Tool");
        inactiveSkill.setCategory("Tools");
        inactiveSkill.setIsActive(false);
    }

    @Test
    void findActiveSkills_shouldReturnOnlyActiveSkillsSortedByNameIgnoreCase() {
        Skill uncategorizedSkill = new Skill();
        uncategorizedSkill.setId(30L);
        uncategorizedSkill.setName("Git");
        uncategorizedSkill.setCategory(null);
        uncategorizedSkill.setIsActive(true);

        when(skillRepository.findAll()).thenReturn(List.of(
                activeSpringSkill,
                inactiveSkill,
                activeJavaSkill,
                uncategorizedSkill
        ));

        List<Skill> result = studentSkillService.findActiveSkills();

        assertEquals(3, result.size());
        assertEquals(List.of("Git", "Java", "Spring Boot"),
                result.stream().map(Skill::getName).toList());
    }

    @Test
    void findGroupedByCategory_shouldGroupSkillsAndFallbackBlankCategoryToOther() {
        Skill noCategorySkill = new Skill();
        noCategorySkill.setId(30L);
        noCategorySkill.setName("Japanese N2");
        noCategorySkill.setCategory("   ");
        noCategorySkill.setIsActive(true);

        StudentSkill s1 = StudentSkill.builder()
                .id(new StudentSkillId(1L, 10L))
                .skill(activeJavaSkill)
                .proficiencyLevel(4)
                .build();
        StudentSkill s2 = StudentSkill.builder()
                .id(new StudentSkillId(1L, 30L))
                .skill(noCategorySkill)
                .proficiencyLevel(3)
                .build();

        when(studentSkillRepository.findAllById_StudentIdOrderBySkill_NameAsc(1L))
                .thenReturn(List.of(s1, s2));

        Map<String, List<StudentSkill>> grouped = studentSkillService.findGroupedByCategory(1L);

        assertEquals(2, grouped.size());
        assertTrue(grouped.containsKey("Backend"));
        assertTrue(grouped.containsKey("Khác"));
        assertEquals(1, grouped.get("Backend").size());
        assertEquals("Java", grouped.get("Backend").get(0).getSkill().getName());
        assertEquals(1, grouped.get("Khác").size());
        assertEquals("Japanese N2", grouped.get("Khác").get(0).getSkill().getName());
    }

    @Test
    void addSkill_shouldSaveStudentSkillWhenValidAndNotDuplicate() {
        when(studentSkillRepository.existsById_StudentIdAndId_SkillId(1L, 10L)).thenReturn(false);
        when(skillRepository.findById(10L)).thenReturn(Optional.of(activeJavaSkill));

        studentSkillService.addSkill(1L, 10L, 4);

        ArgumentCaptor<StudentSkill> captor = ArgumentCaptor.forClass(StudentSkill.class);
        verify(studentSkillRepository).save(captor.capture());

        StudentSkill saved = captor.getValue();
        assertEquals(new StudentSkillId(1L, 10L), saved.getId());
        assertEquals(1L, saved.getStudentId());
        assertEquals(10L, saved.getSkillId());
        assertEquals(activeJavaSkill, saved.getSkill());
        assertEquals(4, saved.getProficiencyLevel());
    }

    @Test
    void addSkill_shouldThrowWhenSkillAlreadyExistsForStudent() {
        when(studentSkillRepository.existsById_StudentIdAndId_SkillId(1L, 10L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> studentSkillService.addSkill(1L, 10L, 3)
        );

        assertTrue(ex.getMessage().contains("đã có"));
        verify(studentSkillRepository, never()).save(any());
    }

    @Test
    void addSkill_shouldThrowWhenDataIntegrityViolationOccurs() {
        when(studentSkillRepository.existsById_StudentIdAndId_SkillId(1L, 10L)).thenReturn(false);
        when(skillRepository.findById(10L)).thenReturn(Optional.of(activeJavaSkill));
        when(studentSkillRepository.save(any())).thenThrow(new DataIntegrityViolationException("Duplicate PK"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> studentSkillService.addSkill(1L, 10L, 3)
        );

        assertTrue(ex.getMessage().contains("đã có"));
    }

    @Test
    void addSkill_shouldThrowWhenSkillIsInactiveOrNotFound() {
        when(studentSkillRepository.existsById_StudentIdAndId_SkillId(1L, 99L)).thenReturn(false);
        when(skillRepository.findById(99L)).thenReturn(Optional.of(inactiveSkill));

        assertThrows(
                IllegalArgumentException.class,
                () -> studentSkillService.addSkill(1L, 99L, 3)
        );
        verify(studentSkillRepository, never()).save(any());
    }

    @Test
    void addSkill_shouldThrowWhenProficiencyLevelOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> studentSkillService.addSkill(1L, 10L, 0));
        assertThrows(IllegalArgumentException.class, () -> studentSkillService.addSkill(1L, 10L, 6));
        assertThrows(IllegalArgumentException.class, () -> studentSkillService.addSkill(1L, 10L, null));

        verifyNoInteractions(studentSkillRepository, skillRepository);
    }

    @Test
    void updateLevel_shouldUpdateProficiencyLevelWhenExisting() {
        StudentSkill existing = StudentSkill.builder()
                .id(new StudentSkillId(1L, 10L))
                .skill(activeJavaSkill)
                .proficiencyLevel(2)
                .build();

        when(studentSkillRepository.findById_StudentIdAndId_SkillId(1L, 10L))
                .thenReturn(Optional.of(existing));

        studentSkillService.updateLevel(1L, 10L, 5);

        assertEquals(5, existing.getProficiencyLevel());
        verify(studentSkillRepository).save(existing);
    }

    @Test
    void updateLevel_shouldThrowWhenNotFound() {
        when(studentSkillRepository.findById_StudentIdAndId_SkillId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> studentSkillService.updateLevel(1L, 10L, 4)
        );
        verify(studentSkillRepository, never()).save(any());
    }

    @Test
    void removeSkill_shouldDeleteWhenExisting() {
        StudentSkill existing = StudentSkill.builder()
                .id(new StudentSkillId(1L, 10L))
                .skill(activeJavaSkill)
                .proficiencyLevel(3)
                .build();

        when(studentSkillRepository.findById_StudentIdAndId_SkillId(1L, 10L))
                .thenReturn(Optional.of(existing));

        studentSkillService.removeSkill(1L, 10L);

        verify(studentSkillRepository).delete(existing);
    }

    @Test
    void removeSkill_shouldThrowWhenNotFound() {
        when(studentSkillRepository.findById_StudentIdAndId_SkillId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> studentSkillService.removeSkill(1L, 10L)
        );
        verify(studentSkillRepository, never()).delete(any());
    }
}
