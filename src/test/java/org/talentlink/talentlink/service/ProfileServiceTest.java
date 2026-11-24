package org.talentlink.talentlink.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.talentlink.talentlink.dto.ProfileUpdateForm;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void getProfileForm_populatesFieldsFromCurrentUser() {
        User user = new User();
        user.setId(5L);
        user.setEmail("ethan.student@talentlink.local");
        user.setFullName("Ethan Carter");
        user.setDateOfBirth(LocalDate.of(2004, 5, 15));
        user.setPhone("0902000005");
        user.setBio("Quan tâm backend Java");
        user.setDesiredPosition("Backend Intern");
        user.setDesiredLocation("TP. Hồ Chí Minh");

        when(userRepository.findByEmailIgnoreCase("ethan.student@talentlink.local"))
                .thenReturn(Optional.of(user));

        ProfileUpdateForm form = profileService.getProfileForm("ethan.student@talentlink.local");

        assertEquals("Ethan Carter", form.getFullName());
        assertEquals(LocalDate.of(2004, 5, 15), form.getDateOfBirth());
        assertEquals("0902000005", form.getPhone());
        assertEquals("Quan tâm backend Java", form.getBio());
        assertEquals("Backend Intern", form.getDesiredPosition());
        assertEquals("TP. Hồ Chí Minh", form.getDesiredLocation());
    }

    @Test
    void updateProfile_savesNormalizedFieldsForOwner() {
        User user = new User();
        user.setId(2L);
        user.setEmail("bobby.partner@talentlink.local");
        user.setFullName("Bobby Mercer");

        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setFullName("  Bobby Updated  ");
        form.setDateOfBirth(LocalDate.of(1995, 3, 20));
        form.setPhone("  0901234567  ");
        form.setBio("   ");
        form.setDesiredPosition("  Tech Lead  ");
        form.setDesiredLocation("");

        when(userRepository.findByEmailIgnoreCase("bobby.partner@talentlink.local"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        profileService.updateProfile("bobby.partner@talentlink.local", form);

        assertEquals("Bobby Updated", user.getFullName());
        assertEquals(LocalDate.of(1995, 3, 20), user.getDateOfBirth());
        assertEquals("0901234567", user.getPhone());
        assertNull(user.getBio());
        assertEquals("Tech Lead", user.getDesiredPosition());
        assertNull(user.getDesiredLocation());
        verify(userRepository).save(user);
    }

    @Test
    void getCurrentUserByEmail_whenNotFound_throwsException() {
        when(userRepository.findByEmailIgnoreCase("missing@talentlink.local"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> profileService.getCurrentUserByEmail("missing@talentlink.local"));
        verify(userRepository, never()).save(any());
    }
}
