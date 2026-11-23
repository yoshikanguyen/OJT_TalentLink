package org.talentlink.talentlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.talentlink.talentlink.dto.StudentRegisterForm;
import org.talentlink.talentlink.entity.StudentProfile;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.StudentProfileRepository;
import org.talentlink.talentlink.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentRegistrationService studentRegistrationService;

    private StudentRegisterForm validForm;

    @BeforeEach
    void setUp() {
        validForm = new StudentRegisterForm();
        validForm.setFullName("  Nguyen Van A  ");
        validForm.setEmail("  Student@Example.COM  ");
        validForm.setPassword("Secret@123");
        validForm.setConfirmPassword("Secret@123");
    }

    @Test
    void registerStudent_shouldCreateUserAndPrivateEmptyProfile_whenValid() {
        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret@123")).thenReturn("$2a$10$encodedPasswordHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        User savedUser = studentRegistrationService.registerStudent(validForm);

        assertNotNull(savedUser);
        assertEquals("Nguyen Van A", savedUser.getFullName());
        assertEquals("student@example.com", savedUser.getEmail());
        assertEquals("$2a$10$encodedPasswordHash", savedUser.getPasswordHash());
        assertTrue(savedUser.getIsActive());

        ArgumentCaptor<StudentProfile> profileCaptor = ArgumentCaptor.forClass(StudentProfile.class);
        verify(studentProfileRepository, times(1)).save(profileCaptor.capture());

        StudentProfile createdProfile = profileCaptor.getValue();
        assertNotNull(createdProfile);
        assertEquals(savedUser, createdProfile.getUser());
        assertEquals("PRIVATE", createdProfile.getPrivacyStatus());

        verify(passwordEncoder, times(1)).encode("Secret@123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerStudent_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.findByEmailIgnoreCase("student@example.com"))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> studentRegistrationService.registerStudent(validForm)
        );

        assertEquals("Email này đã được sử dụng", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(studentProfileRepository, never()).save(any(StudentProfile.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}
