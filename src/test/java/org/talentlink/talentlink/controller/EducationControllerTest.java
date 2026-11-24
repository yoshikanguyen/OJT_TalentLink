package org.talentlink.talentlink.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.talentlink.talentlink.dto.EducationForm;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.service.EducationService;
import org.talentlink.talentlink.service.ProfileService;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class EducationControllerTest {

    @ControllerAdvice
    static class TestAccessDeniedAdvice {
        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        String handleAccessDenied() {
            return "error/403";
        }
    }

    @Mock
    private EducationService educationService;

    @Mock
    private ProfileService profileService;

    private MockMvc mockMvc;
    private Principal principal;

    @BeforeEach
    void setUp() {
        EducationController controller = new EducationController(educationService, profileService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TestAccessDeniedAdvice())
                .build();
        principal = () -> "ethan.student@talentlink.local";

        User user = new User();
        user.setEmail("ethan.student@talentlink.local");
        lenient().when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(user);
        lenient().when(educationService.getEducationsForUser("ethan.student@talentlink.local")).thenReturn(List.of());
    }

    @Test
    void createEducation_validInput_redirectsToProfile() throws Exception {
        mockMvc.perform(post("/me/profile/educations")
                        .principal(principal)
                        .param("schoolName", "ĐH Bách Khoa")
                        .param("major", "Khoa học máy tính")
                        .param("startDate", "2022-09-01")
                        .param("endDate", "")
                        .param("gpa", "3.50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/me/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(educationService).createEducation(eq("ethan.student@talentlink.local"), any(EducationForm.class));
    }

    @Test
    void createEducation_startDateAfterEndDate_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/me/profile/educations")
                        .principal(principal)
                        .param("schoolName", "ĐH Bách Khoa")
                        .param("major", "Khoa học máy tính")
                        .param("startDate", "2025-06-01")
                        .param("endDate", "2024-06-01")
                        .param("gpa", "3.20"))
                .andExpect(status().isOk())
                .andExpect(view().name("me/education-form"))
                .andExpect(model().attributeHasFieldErrors("educationForm", "endDate"));

        verify(educationService, never()).createEducation(any(), any());
    }

    @Test
    void createEducation_gpaOutOfRange_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/me/profile/educations")
                        .principal(principal)
                        .param("schoolName", "ĐH Bách Khoa")
                        .param("major", "Khoa học máy tính")
                        .param("startDate", "2022-09-01")
                        .param("gpa", "4.50"))
                .andExpect(status().isOk())
                .andExpect(view().name("me/education-form"))
                .andExpect(model().attributeHasFieldErrors("educationForm", "gpa"));

        verify(educationService, never()).createEducation(any(), any());
    }

    @Test
    void showEditForm_otherUsersEducationId_returns403() throws Exception {
        when(educationService.getEducationForm("ethan.student@talentlink.local", 99L))
                .thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(get("/me/profile/educations/99/edit")
                        .principal(principal))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateEducation_otherUsersEducationId_returns403() throws Exception {
        doThrow(new AccessDeniedException("Forbidden"))
                .when(educationService)
                .getOwnedEducation("ethan.student@talentlink.local", 99L);

        mockMvc.perform(post("/me/profile/educations/99")
                        .principal(principal)
                        .param("schoolName", "ĐH Bách Khoa")
                        .param("major", "CNTT")
                        .param("startDate", "2022-09-01"))
                .andExpect(status().isForbidden());

        verify(educationService, never()).updateEducation(any(), any(), any());
    }

    @Test
    void deleteEducation_otherUsersEducationId_returns403() throws Exception {
        doThrow(new AccessDeniedException("Forbidden"))
                .when(educationService)
                .deleteEducation("ethan.student@talentlink.local", 99L);

        mockMvc.perform(post("/me/profile/educations/99/delete")
                        .principal(principal))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEducation_ownEducationId_redirectsToProfile() throws Exception {
        mockMvc.perform(post("/me/profile/educations/10/delete")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/me/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(educationService).deleteEducation("ethan.student@talentlink.local", 10L);
    }
}
