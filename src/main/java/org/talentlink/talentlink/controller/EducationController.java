package org.talentlink.talentlink.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.talentlink.talentlink.dto.EducationForm;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.service.EducationService;
import org.talentlink.talentlink.service.ProfileService;

import java.security.Principal;

@Controller
@RequestMapping("/me/profile/educations")
public class EducationController {

    private final EducationService educationService;
    private final ProfileService profileService;

    public EducationController(EducationService educationService, ProfileService profileService) {
        this.educationService = educationService;
        this.profileService = profileService;
    }

    @GetMapping("/new")
    public String showCreateForm(Principal principal, Model model) {
        populateCommonModel(principal, model, null);
        if (!model.containsAttribute("educationForm")) {
            model.addAttribute("educationForm", new EducationForm());
        }
        model.addAttribute("formAction", "/me/profile/educations");
        model.addAttribute("isEdit", false);
        return "me/education-form";
    }

    @PostMapping
    public String createEducation(
            @Valid @ModelAttribute("educationForm") EducationForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateDateRange(form, bindingResult);
        if (bindingResult.hasErrors()) {
            populateCommonModel(principal, model, null);
            model.addAttribute("formAction", "/me/profile/educations");
            model.addAttribute("isEdit", false);
            return "me/education-form";
        }

        educationService.createEducation(principal.getName(), form);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm học vấn.");
        return "redirect:/me/profile";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Principal principal, Model model) {
        populateCommonModel(principal, model, id);
        if (!model.containsAttribute("educationForm")) {
            model.addAttribute("educationForm", educationService.getEducationForm(principal.getName(), id));
        }
        model.addAttribute("formAction", "/me/profile/educations/" + id);
        model.addAttribute("isEdit", true);
        return "me/education-form";
    }

    @PostMapping("/{id}")
    public String updateEducation(
            @PathVariable Long id,
            @Valid @ModelAttribute("educationForm") EducationForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        educationService.getOwnedEducation(principal.getName(), id);
        validateDateRange(form, bindingResult);

        if (bindingResult.hasErrors()) {
            populateCommonModel(principal, model, id);
            model.addAttribute("formAction", "/me/profile/educations/" + id);
            model.addAttribute("isEdit", true);
            return "me/education-form";
        }

        educationService.updateEducation(principal.getName(), id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật học vấn.");
        return "redirect:/me/profile";
    }

    @PostMapping("/{id}/delete")
    public String deleteEducation(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        educationService.deleteEducation(principal.getName(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xoá học vấn.");
        return "redirect:/me/profile";
    }

    private void validateDateRange(EducationForm form, BindingResult bindingResult) {
        if (form.getStartDate() != null && form.getEndDate() != null
                && form.getStartDate().isAfter(form.getEndDate())) {
            bindingResult.rejectValue("endDate", "invalid.dateRange", "Từ ngày không được sau đến ngày");
        }
    }

    private void populateCommonModel(Principal principal, Model model, Long educationId) {
        User currentUser = profileService.getCurrentUserByEmail(principal.getName());
        model.addAttribute("userEmail", currentUser.getEmail());
        model.addAttribute("educations", educationService.getEducationsForUser(principal.getName()));
        if (educationId != null) {
            model.addAttribute("educationId", educationId);
        }
    }
}
