package org.talentlink.talentlink.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.UserRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SpringSecurityLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    private User buildUser(Long id, String email, String rawPassword, Long roleId, boolean active) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName("Test User");
        user.setRoleId(roleId);
        user.setIsActive(active);
        return user;
    }

    @Test
    void loginAdmin_redirectsToAdminHome() throws Exception {
        User admin = buildUser(1L, "admin@talentlink.edu.vn", "Secret123!", RoleMappingHelper.ADMIN_ROLE_ID, true);
        when(userRepository.findByEmailIgnoreCase("admin@talentlink.edu.vn")).thenReturn(Optional.of(admin));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "admin@talentlink.edu.vn")
                        .param("password", "Secret123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void loginPartner_redirectsToPartnerHome() throws Exception {
        User partner = buildUser(2L, "partner@talentlink.edu.vn", "Secret123!", RoleMappingHelper.PARTNER_ROLE_ID, true);
        when(userRepository.findByEmailIgnoreCase("partner@talentlink.edu.vn")).thenReturn(Optional.of(partner));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "partner@talentlink.edu.vn")
                        .param("password", "Secret123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/partner/dashboard"));
    }

    @Test
    void loginStudent_redirectsToStudentHome() throws Exception {
        User student = buildUser(3L, "student@talentlink.edu.vn", "Secret123!", RoleMappingHelper.STUDENT_ROLE_ID, true);
        when(userRepository.findByEmailIgnoreCase("student@talentlink.edu.vn")).thenReturn(Optional.of(student));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "student@talentlink.edu.vn")
                        .param("password", "Secret123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/me/home"));
    }

    @Test
    void loginLockedAccount_failsAndShowsLockReason() throws Exception {
        User lockedUser = buildUser(4L, "locked@talentlink.edu.vn", "Secret123!", RoleMappingHelper.STUDENT_ROLE_ID, false);
        when(userRepository.findByEmailIgnoreCase("locked@talentlink.edu.vn")).thenReturn(Optional.of(lockedUser));

        var result = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "locked@talentlink.edu.vn")
                        .param("password", "Secret123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn();

        mockMvc.perform(get("/login")
                        .param("error", "")
                        .session((org.springframework.mock.web.MockHttpSession) result.getRequest().getSession()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("khoá")));
    }

    @Test
    void loginWithoutCsrf_isForbidden() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "student@talentlink.edu.vn")
                        .param("password", "Secret123!"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "student@talentlink.edu.vn", roles = "STUDENT")
    void studentAccessingAdminUrl_isForbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "partner@talentlink.edu.vn", roles = "PARTNER")
    void partnerAccessingStudentUrl_isForbidden() throws Exception {
        mockMvc.perform(get("/student/my-projects"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@talentlink.edu.vn", roles = "ADMIN")
    void logoutWithCsrf_redirectsToLoginLogout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }
}
