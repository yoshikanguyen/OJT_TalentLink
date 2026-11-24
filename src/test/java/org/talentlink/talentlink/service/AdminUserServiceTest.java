package org.talentlink.talentlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.talentlink.talentlink.entity.Company;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.CompanyRepository;
import org.talentlink.talentlink.repository.UserRepository;
import org.talentlink.talentlink.security.RoleMappingHelper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userRepository, companyRepository, passwordEncoder);
    }

    @Test
    void createPartnerAccount_createsUserAndCompanyWhenNotExists() {
        when(userRepository.findByEmailIgnoreCase("hr@acme.vn")).thenReturn(Optional.empty());
        when(companyRepository.findByNameIgnoreCase("Acme Corp")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("Secret@123")).thenReturn("ENCODED_PASS");

        adminUserService.createPartnerAccount(
                "Nguyen Van A",
                "hr@acme.vn",
                "Secret@123",
                "0901234567",
                "Acme Corp"
        );

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        assertEquals("Acme Corp", companyCaptor.getValue().getName());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("Nguyen Van A", saved.getFullName());
        assertEquals("hr@acme.vn", saved.getEmail());
        assertEquals("ENCODED_PASS", saved.getPasswordHash());
        assertEquals("0901234567", saved.getPhone());
        assertEquals(RoleMappingHelper.PARTNER_ROLE_ID, saved.getRoleId());
        assertTrue(saved.getIsActive());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void createPartnerAccount_duplicateEmail_throwsException() {
        when(userRepository.findByEmailIgnoreCase("dup@acme.vn")).thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> adminUserService.createPartnerAccount("Partner", "dup@acme.vn", "pass123", null, "Acme")
        );

        assertTrue(ex.getMessage().contains("Email"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void toggleUserActiveStatus_preventSelfLock_throwsIllegalStateException() {
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@talentlink.org");
        admin.setIsActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(
                IllegalStateException.class,
                () -> adminUserService.toggleUserActiveStatus(1L, "ADMIN@talentlink.org")
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void toggleUserActiveStatus_locksOtherActiveUser() {
        User target = new User();
        target.setId(5L);
        target.setEmail("student@talentlink.org");
        target.setIsActive(true);

        when(userRepository.findById(5L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean nextActive = adminUserService.toggleUserActiveStatus(5L, "admin@talentlink.org");

        assertFalse(nextActive);
        assertFalse(target.getIsActive());
        verify(userRepository).save(target);
    }

    @Test
    void getUsers_mapsStatusAndDelegatesToRepository() {
        Page<User> mockPage = new PageImpl<>(List.of(), PageRequest.of(1, 5), 0);
        when(userRepository.searchUsers(eq("nghi"), eq(3L), eq(Boolean.FALSE), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<User> result = adminUserService.getUsers("  nghi  ", 3L, "locked", 1, 5);

        assertSame(mockPage, result);
        verify(userRepository).searchUsers(eq("nghi"), eq(3L), eq(Boolean.FALSE), any(Pageable.class));
    }
}
