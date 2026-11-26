package org.talentlink.talentlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.talentlink.talentlink.dto.ProfileUpdateForm;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.StudentProfileRepository;
import org.talentlink.talentlink.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private AvatarStorageService avatarStorageService;

    @InjectMocks
    private ProfileService profileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(5L);
        user.setEmail("ethan.student@talentlink.local");
        user.setFullName("Ethan Carter");
        user.setPhone("0902000005");
        user.setAvatarUrl("/uploads/avatars/old-avatar.png");
    }

    @Test
    void updateProfile_withNewAvatar_savesNewAvatarUpdatesUserAndDeletesOldAvatar() {
        when(userRepository.findByEmailIgnoreCase("ethan.student@talentlink.local"))
                .thenReturn(Optional.of(user));
        when(avatarStorageService.saveAvatar(any()))
                .thenReturn("/uploads/avatars/new-avatar.png");

        MockMultipartFile newFile = new MockMultipartFile(
                "avatarFile",
                "new.png",
                "image/png",
                "new-image".getBytes()
        );

        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setFullName("  Ethan Updated  ");
        form.setPhone("0901234567");
        form.setAvatarFile(newFile);

        profileService.updateProfile("ethan.student@talentlink.local", form);

        assertEquals("Ethan Updated", user.getFullName());
        assertEquals("/uploads/avatars/new-avatar.png", user.getAvatarUrl());
        verify(avatarStorageService).saveAvatar(newFile);
        verify(userRepository).save(user);
        verify(avatarStorageService).deleteAvatarIfExists("/uploads/avatars/old-avatar.png");
    }

    @Test
    void updateProfile_withRemoveAvatar_clearsAvatarUrlAndDeletesOldAvatar() {
        when(userRepository.findByEmailIgnoreCase("ethan.student@talentlink.local"))
                .thenReturn(Optional.of(user));

        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setFullName("Ethan Carter");
        form.setRemoveAvatar(true);

        profileService.updateProfile("ethan.student@talentlink.local", form);

        assertNull(user.getAvatarUrl());
        verify(avatarStorageService, never()).saveAvatar(any());
        verify(userRepository).save(user);
        verify(avatarStorageService).deleteAvatarIfExists("/uploads/avatars/old-avatar.png");
    }

    @Test
    void updateProfile_withoutNewFileOrRemoveFlag_keepsExistingAvatar() {
        when(userRepository.findByEmailIgnoreCase("ethan.student@talentlink.local"))
                .thenReturn(Optional.of(user));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "avatarFile",
                "",
                "application/octet-stream",
                new byte[0]
        );

        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setFullName("Ethan Carter");
        form.setAvatarFile(emptyFile);
        form.setRemoveAvatar(false);

        profileService.updateProfile("ethan.student@talentlink.local", form);

        assertEquals("/uploads/avatars/old-avatar.png", user.getAvatarUrl());
        verify(avatarStorageService, never()).saveAvatar(any());
        verify(avatarStorageService, never()).deleteAvatarIfExists(any());
        verify(userRepository).save(user);
    }
}
