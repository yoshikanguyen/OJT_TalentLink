package org.talentlink.talentlink.service;

import org.talentlink.talentlink.entity.Skill;
import org.talentlink.talentlink.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    @Test
    void findAll_shouldReturnOrderedSkills() {
        List<Skill> mockList = List.of(
                Skill.builder().id(1L).name("Java").category("Backend").isActive(true).build(),
                Skill.builder().id(2L).name("React").category("Frontend").isActive(true).build()
        );
        when(skillRepository.findAllByOrderByIdAsc()).thenReturn(mockList);

        List<Skill> result = skillService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        verify(skillRepository).findAllByOrderByIdAsc();
    }

    @Test
    void create_shouldTrimAndSaveNewSkill() {
        when(skillRepository.existsByNameIgnoreCase("Spring Boot")).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });

        Skill created = skillService.create("  Spring Boot  ", "  Backend  ");

        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getName()).isEqualTo("Spring Boot");
        assertThat(created.getCategory()).isEqualTo("Backend");
        assertThat(created.getIsActive()).isTrue();
    }

    @Test
    void create_shouldSetBlankCategoryToNull() {
        when(skillRepository.existsByNameIgnoreCase("Docker")).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Skill created = skillService.create("Docker", "   ");

        assertThat(created.getCategory()).isNull();
    }

    @Test
    void create_shouldThrowWhenNameIsBlank() {
        assertThatThrownBy(() -> skillService.create("   ", "Backend"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("không được để trống");

        verify(skillRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenNameAlreadyExists() {
        when(skillRepository.existsByNameIgnoreCase("Java")).thenReturn(true);

        assertThatThrownBy(() -> skillService.create("Java", "Backend"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã tồn tại");

        verify(skillRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateSkillWhenValid() {
        Skill existing = Skill.builder().id(1L).name("Java").category("Old").isActive(true).build();
        when(skillRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(skillRepository.existsByNameIgnoreCaseAndIdNot("Java 17", 1L)).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Skill updated = skillService.update(1L, "Java 17", "Backend");

        assertThat(updated.getName()).isEqualTo("Java 17");
        assertThat(updated.getCategory()).isEqualTo("Backend");
    }

    @Test
    void update_shouldThrowWhenDuplicateNameOnOtherSkill() {
        Skill existing = Skill.builder().id(1L).name("Java").category("Backend").isActive(true).build();
        when(skillRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(skillRepository.existsByNameIgnoreCaseAndIdNot("MySQL", 1L)).thenReturn(true);

        assertThatThrownBy(() -> skillService.update(1L, "MySQL", "Database"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã tồn tại");
    }

    @Test
    void toggleActive_shouldFlipBooleanState() {
        Skill existing = Skill.builder().id(1L).name("Java").category("Backend").isActive(true).build();
        when(skillRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        skillService.toggleActive(1L);
        assertThat(existing.getIsActive()).isFalse();

        skillService.toggleActive(1L);
        assertThat(existing.getIsActive()).isTrue();
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.findById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}
