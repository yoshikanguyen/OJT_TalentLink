package org.talentlink.talentlink.controller;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.talentlink.talentlink.dto.ProfileUpdateForm;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.service.ProfileService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    private static Validator validator;

    @Mock
    private ProfileService profileService;

    private ProfileController profileController;
    private Principal principal;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setUp() {
        profileController = new ProfileController(profileService);
        principal = () -> "ethan.student@talentlink.local";
    }

    @Test
    void showProfile_loadsFormAndReturnsView() {
        User currentUser = new User();
        currentUser.setEmail("ethan.student@talentlink.local");

        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setFullName("Ethan Carter");

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(currentUser);
        when(profileService.getProfileForm("ethan.student@talentlink.local")).thenReturn(form);

        Model model = new ExtendedModelMap();
        String view = profileController.showProfile(principal, model);

        assertEquals("me/profile", view);
        assertEquals("ethan.student@talentlink.local", model.getAttribute("userEmail"));
        assertNotNull(model.getAttribute("profileForm"));
    }

    @Test
    void updateProfile_validInput_updatesAndRedirectsWithFlash() {
        User currentUser = new User();
        currentUser.setEmail("ethan.student@talentlink.local");

        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setFullName("Ethan Carter");
        form.setDateOfBirth(LocalDate.of(2004, 1, 10));
        form.setPhone("0902000005");
        form.setDesiredPosition("Java Developer");
        form.setDesiredLocation("TP. Hồ Chí Minh");
        form.setBio("Sinh viên CNTT");

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "profileForm");
        validator.validate(form).forEach(v ->
                bindingResult.rejectValue(v.getPropertyPath().toString(), "", v.getMessage()));

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(currentUser);

        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = profileController.updateProfile(form, bindingResult, principal, model, redirectAttributes);

        assertEquals("redirect:/me/profile", view);
        assertNotNull(redirectAttributes.getFlashAttributes().get("successMessage"));
        verify(profileService).updateProfile(eq("ethan.student@talentlink.local"), eq(form));
    }

    @Test
    void updateProfile_invalidPhoneAndBlankName_returnsViewWithFieldErrors() {
        User currentUser = new User();
        currentUser.setEmail("ethan.student@talentlink.local");

        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setFullName("   ");
        form.setPhone("abc-123");

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "profileForm");
        validator.validate(form).forEach(v ->
                bindingResult.rejectValue(v.getPropertyPath().toString(), "", v.getMessage()));

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(currentUser);

        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = profileController.updateProfile(form, bindingResult, principal, model, redirectAttributes);

        assertEquals("me/profile", view);
        assertTrue(bindingResult.hasFieldErrors("fullName"));
        assertTrue(bindingResult.hasFieldErrors("phone"));
        verify(profileService, never()).updateProfile(any(), any());
    }

    @Test
    void updateProfile_dateTypeMismatch_rejectsDateOfBirthWithCustomMessage() {
        User currentUser = new User();
        currentUser.setEmail("ethan.student@talentlink.local");

        ProfileUpdateForm form = new ProfileUpdateForm();
        form.setFullName("Ethan Carter");

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "profileForm");
        bindingResult.rejectValue("dateOfBirth", "typeMismatch", "Failed to convert property value");

        when(profileService.getCurrentUserByEmail("ethan.student@talentlink.local")).thenReturn(currentUser);

        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = profileController.updateProfile(form, bindingResult, principal, model, redirectAttributes);

        assertEquals("me/profile", view);
        assertTrue(bindingResult.hasFieldErrors("dateOfBirth"));
        List<FieldError> dobErrors = bindingResult.getFieldErrors("dateOfBirth");
        assertFalse(dobErrors.isEmpty());
        assertEquals(
                "Ngày sinh không đúng định dạng (yyyy-MM-dd)",
                dobErrors.get(dobErrors.size() - 1).getDefaultMessage()
        );
        verify(profileService, never()).updateProfile(any(), any());
    }
}