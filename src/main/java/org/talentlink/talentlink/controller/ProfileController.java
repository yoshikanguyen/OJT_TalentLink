package org.talentlink.talentlink.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.talentlink.talentlink.dto.ProfileUpdateForm;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.service.ProfileService;
import org.talentlink.talentlink.service.EducationService;

import java.security.Principal;

@Controller
@RequestMapping("/me/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final EducationService educationService;

    public ProfileController(ProfileService profileService, EducationService educationService) {
        this.profileService = profileService;
        this.educationService = educationService;
    }

    @GetMapping
    public String showProfile(Principal principal, Model model) {
        User currentUser = profileService.getCurrentUserByEmail(principal.getName());
        model.addAttribute("userEmail", currentUser.getEmail());
        model.addAttribute("educations", educationService.getEducationsForUser(principal.getName()));
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", profileService.getProfileForm(principal.getName()));
        }
        return "me/profile";
    }

    @PostMapping
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileUpdateForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasFieldErrors("dateOfBirth")) {
            FieldError dobError = bindingResult.getFieldError("dateOfBirth");
            if (dobError != null && "typeMismatch".equals(dobError.getCode())) {
                bindingResult.rejectValue("dateOfBirth", "invalid.date", "Ngày sinh không đúng định dạng (yyyy-MM-dd)");
            }
        }

        User currentUser = profileService.getCurrentUserByEmail(principal.getName());
        model.addAttribute("userEmail", currentUser.getEmail());

        if (bindingResult.hasErrors()) {
            return "me/profile";
        }

        profileService.updateProfile(principal.getName(), form);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công.");
        return "redirect:/me/profile";
    }
}
