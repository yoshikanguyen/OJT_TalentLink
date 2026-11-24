package org.talentlink.talentlink.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.talentlink.talentlink.dto.EducationForm;
import org.talentlink.talentlink.entity.Education;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.EducationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EducationServiceTest {

    @Mock
    private EducationRepository educationRepository;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private EducationService educationService;

    private User buildUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    @Test
    void getEducationsForUser_returnsCurrentUserEducations() {
        User currentUser = buildUser(1L, "ethan.student@talentlink.local");
        Education edu = new Education();
        edu.setId(10L);
        edu.setUser(currentUser);
        edu.setSchoolName("ĐH Bách Khoa");

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(currentUser);
        when(educationRepository.findByUserIdOrderByStartDateDescIdDesc(1L)).thenReturn(List.of(edu));

        List<Education> result = educationService.getEducationsForUser("ethan.student@talentlink.local");

        assertEquals(1, result.size());
        assertEquals("ĐH Bách Khoa", result.get(0).getSchoolName());
    }

    @Test
    void createEducation_savesEducationWithOptionalEndDateAndGpa() {
        User currentUser = buildUser(1L, "ethan.student@talentlink.local");
        EducationForm form = new EducationForm();
        form.setSchoolName("  ĐH Khoa học Tự nhiên  ");
        form.setMajor("  Công nghệ thông tin  ");
        form.setStartDate(LocalDate.of(2022, 9, 1));
        form.setEndDate(null);
        form.setGpa(null);

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(currentUser);
        when(educationRepository.save(any(Education.class))).thenAnswer(inv -> inv.getArgument(0));

        educationService.createEducation("ethan.student@talentlink.local", form);

        verify(educationRepository).save(any(Education.class));
    }

    @Test
    void updateEducation_whenOwner_updatesFields() {
        User owner = buildUser(1L, "ethan.student@talentlink.local");
        Education existing = new Education();
        existing.setId(5L);
        existing.setUser(owner);
        existing.setSchoolName("Trường cũ");
        existing.setMajor("Ngành cũ");
        existing.setStartDate(LocalDate.of(2020, 9, 1));

        EducationForm form = new EducationForm();
        form.setSchoolName("  ĐH FPT  ");
        form.setMajor("  Kỹ thuật phần mềm  ");
        form.setStartDate(LocalDate.of(2021, 9, 1));
        form.setEndDate(LocalDate.of(2025, 6, 30));
        form.setGpa(new BigDecimal("3.65"));

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(owner);
        when(educationRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(educationRepository.save(any(Education.class))).thenAnswer(inv -> inv.getArgument(0));

        educationService.updateEducation("ethan.student@talentlink.local", 5L, form);

        assertEquals("ĐH FPT", existing.getSchoolName());
        assertEquals("Kỹ thuật phần mềm", existing.getMajor());
        assertEquals(LocalDate.of(2021, 9, 1), existing.getStartDate());
        assertEquals(LocalDate.of(2025, 6, 30), existing.getEndDate());
        assertEquals(new BigDecimal("3.65"), existing.getGpa());
        verify(educationRepository).save(existing);
    }

    @Test
    void updateEducation_whenNotOwner_throwsAccessDenied() {
        User currentUser = buildUser(1L, "ethan.student@talentlink.local");
        User otherUser = buildUser(2L, "other@talentlink.local");

        Education otherEducation = new Education();
        otherEducation.setId(99L);
        otherEducation.setUser(otherUser);

        EducationForm form = new EducationForm();
        form.setSchoolName("Hack School");
        form.setMajor("Hack Major");
        form.setStartDate(LocalDate.of(2022, 1, 1));

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(currentUser);
        when(educationRepository.findById(99L)).thenReturn(Optional.of(otherEducation));

        assertThrows(AccessDeniedException.class,
                () -> educationService.updateEducation("ethan.student@talentlink.local", 99L, form));
        verify(educationRepository, never()).save(any());
    }

    @Test
    void deleteEducation_whenNotOwner_throwsAccessDenied() {
        User currentUser = buildUser(1L, "ethan.student@talentlink.local");
        User otherUser = buildUser(2L, "other@talentlink.local");

        Education otherEducation = new Education();
        otherEducation.setId(99L);
        otherEducation.setUser(otherUser);

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(currentUser);
        when(educationRepository.findById(99L)).thenReturn(Optional.of(otherEducation));

        assertThrows(AccessDeniedException.class,
                () -> educationService.deleteEducation("ethan.student@talentlink.local", 99L));
        verify(educationRepository, never()).delete(any());
    }

    @Test
    void deleteEducation_whenOwner_deletesRecord() {
        User owner = buildUser(1L, "ethan.student@talentlink.local");
        Education existing = new Education();
        existing.setId(7L);
        existing.setUser(owner);

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(owner);
        when(educationRepository.findById(7L)).thenReturn(Optional.of(existing));

        educationService.deleteEducation("ethan.student@talentlink.local", 7L);

        verify(educationRepository).delete(existing);
    }

    @Test
    void getOwnedEducation_whenNotFound_throwsNoSuchElementException() {
        User currentUser = buildUser(1L, "ethan.student@talentlink.local");

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(currentUser);
        when(educationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> educationService.getOwnedEducation("ethan.student@talentlink.local", 404L));
        assertNull(currentUser.getDateOfBirth());
    }
}
